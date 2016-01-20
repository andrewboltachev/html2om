# HTML2Om

Convert HTML to [Om](https://github.com/omcljs/om) layout syntax. For example:

```
<div class="myclass"><p>hello</p><p>world!</p></div>
```

```
(dom/html
 nil
 (dom/body
  nil
  (dom/div
   #js
   {:className "myclass"}
   (dom/p nil "hello")
   (dom/p nil "world!"))))
 ```
 
## How to use
 
HTML2Om is Om Next webapp itself, so to run it locally:
 
```
git clone https://github.com/andrewboltachev/html2om.git
cd html2om
rlwrap lein figwheel
```
 
It would start at port `8701` (I've choosen it to not to interfere with other common port numbers).
It isn't deployed anywhere to the web yet.
 
## TODO

* Add all attributes (for now only `:className` is supported, i.e. `class="myclass"` would be converted to `{:className "myclass"}`, and other attribute names left as-is.
