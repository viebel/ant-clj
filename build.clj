(require '(clojure [string :as string]))
(def apache-lib (str apache-root "javascript/lib/" kona-version "/"))


(deftarget build
          (println "kona.version:" kona-version)
          (run-target :clear)
          (run-target :konalibinline)
          (run-target :newlayer))

(deftarget newlayer
           (run-target :kona-desktop)
           (run-target :kona-mobile))

(deftarget kona-desktop
           (run-target :js-desktop)
           (run-target :flash-desktop))

(deftarget kona-mobile
           (run-target :js-mobile))

(deftarget clear
           (fs/deltree "./build/tmp")
           (fs/mkdir "./build/tmp")
           (fs/deltree apache-lib)
           (fs/mkdir apache-lib))

(defn generate-layers[]
      (defn generate-layer[layer]
        (defn strip[filename]
          (defn strip-line [line]
            (-> line
                (string/replace #"^\s+" "")
                (string/replace #"\s+$" "")
                (string/replace #"'" "\\\\'")
                (string/replace #"\s{2,}" " ")))
          (string/join " " (map strip-line (file-lines filename))))
        
        (let [stripped-html (str "./build/tmp/" layer ".stripped.html")
                            html-content (strip (str layer ".html"))]
          (spit stripped-html html-content)))

      (fs/deltree "./build/resource")
      (fs/mkdir "./build/resource")
      (generate-layer "fullImage"))

(deftarget js-mobile
           (generate-layers)
           (create-file (str apache-lib "KonaMobile.js")
                        (str-from-file-list "bin/compress_mobile.txt")
                        (str-from-file-matched "./build/resource/*Creator.js")))


(deftarget mobile
  (shexec "xmllint"
          (fs/glob "xml-ok/*.xml"))
  (concat-files :srcfile "list.txt" :destfile (str apache-lib "all_in_one.js")
                :header (string/join "\n" ["//This is a generated file"
                                           "//Be careful"
                                           "\n"])
                :footer "//End of generated file\n"))


(deftarget flash-desktop
           )

(deftarget js-desktop
           (shexec "xmllint"
                   (fs/glob "srcFlash/*.js"))
           (create-file (str apache-lib "/KonaFlashBase.js")
                        (str-from-file-list "./bin/compress_flash.txt")))

(deftarget konalibinline
           (create-file (str apache-lib "/KonaLibInline.js")
                        "// auto generated
				if (!window.KONA_VERSION) {
					window.KONA_VERSION = ${kona.version};
				}
				// end auto generated"
                        (str-from-file-list "./bin/compress_konalibinline.txt")))	
