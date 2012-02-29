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
  `(def ~(with-meta name {:target `(fn[] ~@body)})
    (fn[] (run-target (var ~name)))))

(defmacro with-ns [n & body]
  `(let [oldns# *ns*]
    (try
     (in-ns (-> ~n str symbol))
     ~@body
     (finally (in-ns (-> oldns# str symbol))))))

(defn- load-project-files[]
  (with-ns my-ns
           (defn safe-load-file [f]
             (when (fs/exists? f)
               (try (load-file f)
                    (catch Exception e
                           (println "file invalid:" f)
                           (println e)
                           (throw e)))))
           (let [os (-> "os.name" System/getProperty .toLowerCase)]
             (safe-load-file "build.properties.clj")
             (safe-load-file (str "build.properties." os ".clj"))
             (safe-load-file "build.clj"))))


(defn- run-main-target[target]
  (try
   (if-let [res (get-target target)]
           (res)
           (throw (Exception. (str "TARGET NOT FOUND: " target))))
   (finally (shutdown-agents))))

(defn -main [target & args]
  (try
   (load-project-files)
   (run-main-target target)
   (println "BUILD SUCESSFUL")
   (catch Exception e (println "BUILD FAILED" e))))
