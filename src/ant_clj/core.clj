(ns ant-clj.core
    (:gen-class)
    (:use ant-clj.dbg)
    (:use ant-clj.common-targets)
    (:use clojure.stacktrace))

(def my-ns *ns*)

(defn- run-target [target]
  (when-let [t (:target (meta target))]
            (t)))

(defn- get-target[target]
  (when-let [t (ns-resolve my-ns (symbol target))]
            (var-get t)))

(defmacro deftarget[name & body]
  `(def ~(with-meta name {:target `(fn[] (and ~@body))})
    (fn[] (run-target (var ~name)))))

(defmacro with-ns [n & body]
  `(let [oldns# *ns*]
     (in-ns ~n)
     ~@body
     (in-ns (-> oldns# str symbol))))

(defn- load-project-files[]
  (with-ns 'ant-clj.core
           (defn safe-load-file [f]
             (when (fs/exists? f)
               (try (load-file f) true
                    (catch Exception e
                           (println "file invalid:" f)
                           (println e)
                           false))))
           (let [os (-> "os.name" System/getProperty .toLowerCase)]
             (and (safe-load-file "build.properties.clj")
                  (safe-load-file (str "build.properties." os ".clj"))
                  (safe-load-file "build.clj")))))


(defn- execute-target[target]
  (try
   (if-let [res (get-target target)]
           (if (res)
               (do (println "BUILD SUCESSFUL")
                   true)
               (println "BUILD FAILED"))
           (println "TARGET NOT FOUND: " target))
   (catch Exception e (println e))
   (finally (shutdown-agents))))

(defn -main [target & args]
  (and (load-project-files)
       (execute-target target)))
