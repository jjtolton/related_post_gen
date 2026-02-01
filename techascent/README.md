# Techascent (ham-fisted)

Clojure implementation using [ham-fisted](https://github.com/cnuernber/ham-fisted) and [charred](https://github.com/cnuernber/charred) libraries.

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
  │ 5k    │ 36-39ms   │
  ├───────┼───────────┤
  │ 20k   │ 460-531ms │
  ├───────┼───────────┤
  │ 60k   │ 3.76-3.82s│
  └───────┴───────────┘
```
