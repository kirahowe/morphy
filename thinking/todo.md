# TODO

# Site generation

- [ ] Collect partials and pass them to mustache renderer
  - override partials in subdirectories -- closest one wins
  - pass all partials found
  - look in `_partials` directory
  - partials = {:file-name-minus-ext "partial template" ,,,}

- [ ] Support custom templates
  - specify the name of a template in the front-matter (instead of using the
    default one)

- [ ] Support overriding partials in child directories
  - closest partial gets used
  - also support no partials

- [ ] Support non-html mustache templates
  - file-name.md.mustache would get treated like a mustache template/partial,
    but also processed as markdown first before being templated
  - always pass mustache html, but allow for other processing first

- [ ] Pass a tree of pages to templates as `pages`
  - so templates can loop through them
    - {:root [,,,]
      :child [,,,]
      :child.child-of-child [,,,]}


- [ ] Explode loose pages into directories with an index in them
  - so the url doesn't need to end with .html

- [ ] Handle non-happy-path cases
  - more deeply nested directories
    - i.e. have an empty directory level in a path
  - missing template vars at mustache compile time
  - missing template files
  - use generated tests to come up with dummy data

-----

- [ ] Do something about duplicate slugs
  - fail?
  - warn and add a number to them?

- [ ] Support a way to pass meta-data other than front-matter
  - so that people can have pure html/md/txt/etc files without weird stuff at
    the top

# Better code

- [ ] Spec things
  - generate property-based tests from these specs


# CLI

- [ ] Make a CLI
  - extract core part that takes in a context and writes the files
  - `cli build` to compile whole website into output dir

- [ ] Development server
  - something like `cli dev` to run a local server that pick up file changes and
    re-builds the website on the fly while developing


# Documentation

- [ ] readme
- [ ] website
  - installation
  - getting started
  - things to note:
    - must have at least a base file template
    - `content` var will be available here as _already-rendered-html_ -- need to
      escape it in mustache template
- [ ] cli `help`
  - cli usage
