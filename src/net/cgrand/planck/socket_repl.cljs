(ns net.cgrand.planck.socket-repl
  (:require
    [net.cgrand.cljs.js.repl.io :as io]
    [net.cgrand.cljs.js.repl.dynvars :as dyn]
    [net.cgrand.cljs.js.repl.main :as m]
    [cljs.js :as cljs]
    [planck.core]
    [planck.socket.alpha :as socket]))

(defn socket-printer [socket]
  (fn [data]
    (socket/write socket data)))

(defn eval [form cb]
  (cb (planck.core/eval form)))

(defn accept [socket]
  #_(.setEncoding socket "utf8")
  (let [{:keys [in print-fn]} (io/pipe)
        data-handler (fn [socket data]
                       (if data
                         (print-fn data)
                         (print-fn)))
        in (io/line-col-reader in)
        print-fn (socket-printer socket)]
    (dyn/binding [io/*in* in
                  *print-fn* print-fn
                  *print-err-fn* print-fn]
                 (m/repl :eval eval :exit #(socket/close socket))
                 data-handler)))

(defn start-server [port]
  (dyn/binding
    [*ns* *ns*
     cljs.js/*eval-fn* cljs.js/*eval-fn*]
    (socket/listen port (dyn/bound-fn [socket] (accept socket)))))
