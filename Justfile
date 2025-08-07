help:
    just --list

run:
    clojure -M -m example.main

nrepl:
    clojure -M:dev -m nrepl.cmdline

format_check:
    clojure -M:format -m cljfmt.main check src dev test

format:
    clojure -M:format -m cljfmt.main fix src dev test

lint:
    clojure -M:lint -m clj-kondo.main --lint .

test:
    clojure -M:test

outdated:
    clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core

build-fe:
    npm run css:build && npx shadow-cljs release frontend
