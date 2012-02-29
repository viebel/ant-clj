(ns ant-clj.core
    (:use ant-clj.common-targets))

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

(defn- load-project-files[]
  (defn safe-load-file [f]
    (when (fs/exists? f)
      (load-file f)))
  (let [os (-> "os.name" System/getProperty .toLowerCase)]
    (safe-load-file "build.properties.clj")
    (safe-load-file (str "build.properties." os ".clj"))
    (safe-load-file "build.clj")))

(load-project-files); this must be executed in current namespace

(defn -main [target & args]
  (try
   (if-let [res (get-target target)]
           (do (if (res)
                   (println "BUILD SUCESSFUL")
                   (println "BUILD FAILED"))
               (shutdown-agents))
           (println "TARGET NOT FOUND: " target))
   (catch Exception e (println e))))
