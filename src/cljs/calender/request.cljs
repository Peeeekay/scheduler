(ns calender.request
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as client]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [cljs.core.async :refer [<!]]
            [accountant.core :as accountant]))

(defn send-data [body]
  (let [{:keys [month date start end dur data]} body]
  (client/post "http://localhost:3449/save"
      {:json-params {:month month :date date :start-time start :end-time end :duration dur :data data}})))

(defn fetch-data []
  (let [data (client/post "http://localhost:3449/fetch"{:json-params {:month "MAR"}})] data))

