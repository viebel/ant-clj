(ns ant-clj.dbg)

(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))
