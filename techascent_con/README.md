# Techascent Concurrent (ham-fisted)

Concurrent Clojure implementation using [ham-fisted](https://github.com/cnuernber/ham-fisted) and [charred](https://github.com/cnuernber/charred) libraries.

## Usage

```
make
```

Runs clean, build, and AWS benchmark simulation.

Times I got from running `make`:

```
  ┌───────┬───────────┐
  │ Posts │   Time    │
  ├───────┼───────────┤
  │ 5k    │ 34-46ms   │
  ├───────┼───────────┤
  │ 20k   │ 199-260ms │
  ├───────┼───────────┤
  │ 60k   │ 1.3-1.6s  │
  └───────┴───────────┘
```
