(ns related.core
  (:require [charred.api :as charred]
            [ham-fisted.api :as hf]
            [clojure.java.io :as io])
  (:import [java.util HashMap LinkedHashMap List]
           [ham_fisted ArraySection])
  (:gen-class))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def ^:const input-file "../posts.json")
(def ^:const output-file "../related_posts_techascent_con.json")

(defn make-tag-map
  ^HashMap [^objects posts]
  (let [n (alength posts)
        ^HashMap tag-map (HashMap. 128)]
    (dotimes [i n]
      (let [^java.util.Map post (aget posts i)
            ^List tags (.get post "tags")
            ntags (.size tags)]
        (dotimes [j ntags]
          (let [^String tag (.get tags j)
                ^ham_fisted.ArrayLists$IntArrayList lst
                (or (.get tag-map tag)
                    (let [new-lst (hf/int-array-list)]
                      (.put tag-map tag new-lst)
                      new-lst))]
            (.addLong lst i)))))
    tag-map))

(defn get-top-5
  ^longs [^ints tagged-post-count ^long n]
  (let [top5 (long-array 10)]
    (loop [i (long 0)
           min-tags (long 0)]
      (if (< i n)
        (let [cnt (long (aget tagged-post-count i))]
          (if (> cnt min-tags)
            (let [up (long (loop [upper-bound (long 6)]
                             (if-not (and (>= upper-bound 0)
                                          (> cnt (aget top5 upper-bound)))
                               upper-bound
                               (recur (- upper-bound 2)))))]
              (when (< up 6)
                (System/arraycopy top5 (int (+ 2 up)) top5 (int (+ 4 up)) (int (- 6 up))))
              (aset top5 (int (+ up 2)) cnt)
              (aset top5 (int (+ up 3)) i)
              (recur (inc i) (aget top5 8)))
            (recur (inc i) min-tags)))
        top5))))

(defn process-post
  [^objects posts post-idx ^HashMap tag-map ^ints counts]
  (let [n (alength posts)
        post-idx (int post-idx)
        ^java.util.Map post (aget posts post-idx)
        ^List tags (.get post "tags")
        ntags (.size tags)]
    (java.util.Arrays/fill counts (int 0))
    (dotimes [i ntags]
      (let [^String tag (.get tags i)
            ^ham_fisted.ArrayLists$IntArrayList indices (.get tag-map tag)
            ^ArraySection section (.getArraySection indices)
            ^ints arr (.-array section)
            sz (.-eidx section)]
        (dotimes [j sz]
          (let [idx (aget arr j)]
            (aset counts idx (unchecked-inc-int (aget counts idx)))))))
    (aset counts post-idx (int 0))
    (let [^longs top5 (get-top-5 counts n)
          ^objects related (object-array 5)]
      (dotimes [i 5]
        (aset related i (aget posts (int (aget top5 (int (+ 1 (* i 2))))))))
      (doto (LinkedHashMap.)
        (.put "_id" (.get post "_id"))
        (.put "tags" tags)
        (.put "related" (hf/->random-access related))))))

(defn get-all-related-posts [^objects posts]
  (let [n (alength posts)
        tag-map (make-tag-map posts)
        results (object-array n)]
    (dorun
      (hf/pgroups
        n
        (fn [^long start ^long end]
          (let [counts (int-array n)]
            (loop [i start]
              (when (< i end)
                (aset results i (process-post posts i tag-map counts))
                (recur (inc i))))))))
    results))

(defn -main []
  (try
    (let [posts-vec (with-open [rdr (io/reader input-file)]
                      (charred/read-json rdr))
          posts ^objects (into-array Object posts-vec)
          n-posts (int (alength posts))

          _ (let [warmup-posts (into-array Object (take (min 5000 n-posts) posts-vec))]
              (dotimes [_ 3]
                (get-all-related-posts warmup-posts))
              (System/gc))

          t1 (System/currentTimeMillis)
          results (get-all-related-posts posts)
          t2 (System/currentTimeMillis)]

      (println (format "Processing time (w/o IO): %sms" (- t2 t1)))
      (with-open [wtr (io/writer output-file)]
        (charred/write-json wtr (hf/->random-access results))))

    (catch Exception e
      (.printStackTrace e))))
