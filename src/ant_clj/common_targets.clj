(ns ant-clj.common-targets
    (:use clojure.java.shell)
    (:use clojure.java.io)
    (:use ant-clj.dbg)
    (:require [fs])
    (:require [clojure.string :as string]))

(defn file-lines [f]
      (line-seq (reader f)))

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

(defn str-from-fileset [fileset]
  (string/join (map slurp fileset)))

(defn str-from-file-matched [matcher-str]
  (str-from-fileset (fs/glob matcher-str)))

(defn str-from-file-list[srcfile]
  (str-from-fileset (read-file-list srcfile)))

(defn replace-each-line [content & replacements]
      (let [lines (string/split content #"\n")
                  replacement-list (partition 2 replacements)]
        (string/join "\n" (for [line lines]
                               (reduce (fn[res [match replacement]] (string/replace res match replacement)) line replacement-list)))))

(defn join-lines [content sep]
          (string/join sep (string/split content #"\n")))


(defn create-file[f & args]
      (println "create-file:" f)
      (spit f (string/join args)))

(defn concat-files [& {:keys [srcfile destfile header footer]}]
  (println "concat:")
  (let [content (str-from-file-list srcfile)]
    (spit destfile (str header content footer))))

