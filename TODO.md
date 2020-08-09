# TODO

# To triage

- [ ] partials containing partials?

# Site generation

- [ ] Add sorting -- by date? by sort key?
- [ ] Ignore file system crap e.g. `.DS_Store` files
  - ignore all dot files?

---

- [x] Collect partials and pass them to mustache renderer
- [x] Support nested directories of partials
- [x] Support custom templates
  - specify the name of a template in the front-matter (instead of using the
    default one)
- [x] Support overriding partials in child directories
  - closest partial gets used
  - also support no partials
- [x] Support non-html mustache templates
  - file-name.md.mustache would get treated like a mustache template/partial,
    but also processed as markdown first before being templated
  - always pass mustache html, but allow for other processing first
- [x] Pass a tree of pages to templates as `pages`
- [x] Expand loose pages into directories with an index in them
  - so the url doesn't need to end with .html
- [x] Support already-expanded directories

## Error cases to test

- [ ] more deeply nested directories
  - i.e. have an empty directory level in a path
- [ ] missing template vars at mustache compile time
- [ ] missing template files
  - require at least top-level layout
- [ ] missing partials
- [ ] use generated tests to come up with dummy data

- [ ] Do something about duplicate slugs
  - fail?
  - warn and add a number to them?

-----

- [ ] Support a way to pass meta-data other than front-matter?
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

To document:
- default sorting order
- assumed locations of partials, layout
- how mustache files get rendered
  - rendering contexts (site and individual page attrs)
    - layouts get page as rendering context, individual pages get whole site
- vars available in templates (children etc.)
  - rendering context gets passed on to partials
- slug and canonical slug
  - files automatically get put in a directory with an index so urls don't need
    to have .html at the end, but this means the same content lives at two urls
- some default layout/template setups
  - include an rss feed, sitemap

# Before letting anyone else use

- namespace keywords to avoid conflicts with any user metadata ones
- document conventions about naming metadata keys
