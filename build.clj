(require '(clojure [string :as string]))
(def apache-lib (str apache-root "javascript/lib/"))


(deftarget build
          (println "kona.version:" kona-version)
          (run-target "mobile"))

(deftarget mobile
  (shexec "xmllint"
          (fs/glob "xml-ok/*.xml"))
  (concat-files :srcfile "list.txt" :destfile (str apache-lib "all_in_one.js")
                :header (string/join "\n" ["//This is a generated file"
                                           "//Be careful"
                                           "\n"])
                :footer "//End of generated file\n"))

