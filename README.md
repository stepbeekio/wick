# Wick

Wick is a clojure webapp starter template based off of the excellent https://caveman.mccue.dev/

I'm creating this mainly as a personal starter template for new projects. Right now it's pretty bare bones.

## Problems to solve

I really just want a way that I can drop into creating a basic SaaS product quickly. To that end, I'll probably add some of the common trappings like user management etc. eventually.

For now, I'm primarily interested in solving technical problems I'm facing as a clojure n00b.

TODO:
* Implement auth using buddy
* Integrate shadow-cljs and stimulusjs to provide stucture to support basic scripting. Included in this will be figuring out how to hook it all up together.
* Implement a component system that allows a true data-oriented approach based around hiccup.
* Add some more tooling from the clojure ecosystem like portal
* implement something like storybook for UI components - I've no idea what this will look like? MAybe the thing by the replicant guy is available?

## Getting Started

First, run docker compose to start dependencies like the database.

To get started, you can use emacs with cider as the editor and use SPC-m-' to jack in to the clojure repl. Once done, you should open the dev/user.clj file to get started. There you'll be able to start a local server.  


