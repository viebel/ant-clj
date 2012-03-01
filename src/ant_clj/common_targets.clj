(ns ant-clj.common-targets
    (:use clojure.java.shell)
    (:use clojure.java.io)
    (:use ant-clj.dbg)
    (:require [fs])
    (:require [clojure.string :as string]))

(defn- read-file-list [srcfile]
  (defn remove-trailing-spaces[s] (string/replace s #"\s+$" ""))
  (->> srcfile
       reader
       line-seq
       (map remove-trailing-spaces)
       (remove empty?)))

(defn shexec[executable files]
  (defn exec-and-touch[executable src target]
    (let [res (sh executable src)]
      (when (= 0 (:exit res)) (fs/touch target))
      res))
  (defn print-output[output]
    (println (string/join "\n" output)))
  (defn exec[executable filename]
    (defn newer? [a b]
      (apply > (map fs/mtime [a b])))
    (let [target (str filename ".touch")]
      (if (newer? target filename)
          [0 ""]
          (let [{:keys [exit err]} (exec-and-touch executable filename (str filename ".touch"))]
            [exit (str filename ": " (if (= 0 exit) "OK" (str "FAILED\n" err)))]))))
  
  (println (str executable ":"))
  (loop [files files output [] errorcode 0]
    (if (or (empty? files) (not= 0 errorcode))
        (do (print-output output)
            (when (not= 0 errorcode)
              (throw (Exception. ""))))
        (let [filename (first files)
                       [errorcode msg] (exec executable filename)]
          (recur (rest files) (conj output msg) errorcode)))))

(defn concat-files [& opts]
  (println "concat:")
  (let [{:keys [srcfile destfile header footer]} (apply hash-map opts)
               content (string/join (map slurp (read-file-list srcfile)))]
    (spit destfile (str header content footer))))

