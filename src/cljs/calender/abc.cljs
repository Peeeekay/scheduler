(ns calender.abc
  (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [goog.string :as gstring]
              [goog.string.format :as format]
              [clojure.string :as str]
              [calender.state :refer [initial-state]]
              [calender.component :refer [table select-input]]
              [mount.core :refer [defstate]]))

(defn create []
  {:id 1})

(defn stop []
  (js/console.log "nver"))

(defstate conn
  :start (fn [] {:id 2})
  :stop (stop))
