(ns ant-clj.core
    (:gen-class)
    (:use ant-clj.dbg)
    (:use ant-clj.common-targets)
    (:use clojure.stacktrace))

(def my-ns *ns*)

(defn- execute-target-in-var [target]
  (when-let [t (:target (meta target))]
            (println (str (:name (meta target)) ":"))
            (t)))

(defmulti get-target (fn [target] (let [t (type target)]
                                    (cond 
                                    (=  t String) :string
                                    (isa? t clojure.lang.Named) :named
                                    :else :unsupported))))
(defmethod get-target :string [target]
  (when-let [t (ns-resolve my-ns (symbol target))]
            (var-get t)))
(defmethod get-target :named [target]
  (get-target (name target)))

(defmacro deftarget[n & body]
  `(def ~(with-meta n {:target `(fn[] ~@body)})
    (fn[] (execute-target-in-var (var ~n)))))

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
               (load-file f)))

           (let [os (-> "os.name" System/getProperty .toLowerCase)]
             (safe-load-file "build.properties.clj")
             (safe-load-file (str "build.properties." os ".clj"))
             (load-file "build.clj"))))

(defn run-target[target]
   (if-let [res (get-target target)]
           (res)
           (throw (Exception. (str "TARGET NOT FOUND: " target)))))

(defn- run-main-target[target]
  (try (run-target target)
   (finally (shutdown-agents))))

(defn -main [target & args]
  (try
   (load-project-files)
   (run-main-target target)
   (println "BUILD SUCESSFUL")
   (catch Exception e (println (.getMessage e) "\nBUILD FAILED" ))))
