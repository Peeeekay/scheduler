(ns calender.request
  (:require [cljs-http.client :as client]))

(defn send-data [body]
  (let [{:keys [month date start end dur data]} body]
  (client/post "http://localhost:3449/save"
      {:json-params {:month month :date date :start-time start :end-time end :duration dur :data data}})))

