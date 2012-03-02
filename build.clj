(require '(clojure [string :as string]))
(def apache-lib (str apache-root "javascript/lib/" kona-version "/"))


(deftarget build
          (println "kona.version:" kona-version)
          (run-target :clear)
          (run-target :konalibinline))

(deftarget clear
           (fs/deltree apache-lib)
           (fs/mkdir apache-lib))

(deftarget mobile
  (shexec "xmllint"
          (fs/glob "xml-ok/*.xml"))
  (concat-files :srcfile "list.txt" :destfile (str apache-lib "all_in_one.js")
                :header (string/join "\n" ["//This is a generated file"
                                           "//Be careful"
                                           "\n"])
                :footer "//End of generated file\n"))


(deftarget konalibinline
           (create-file (str apache-lib "/KonaLibInline.js")
                        "// auto generated
				if (!window.KONA_VERSION) {
					window.KONA_VERSION = ${kona.version};
				}
				// end auto generated"
                        (str-from-file-list "list.txt")))	
