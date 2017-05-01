(ns calender.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [goog.string :as gstring]
              [goog.string.format :as format]
              [clojure.string :as str]
              [calender.state :refer [initial-state]]
              [calender.component :refer [title]]
              [calender.container :refer [table-container form-container]]))

;;initializign js object

(defn ^:private parse-int [s]
  (let [n (js/parseInt s 10)]
    (if-not (js/isNaN n)
      n)))

(def TITLE (js-obj
  "JAN" "JANUARY" "FEB" "FEBRUARY" "MAR" "MARCH" "APR" "APRIL"
  "MAY" "MAY" "JUNE" "JUNE" "JULY" "JULY" "AUG" "AUGUST"
  "SEPT" "SEPTEMBER" "OCT" "OCTOBER" "NOV" "NOVEMBER" "DEC" "DECEMBER"))

(defn is-uppercase [s]
  (= s (str/upper-case s)))

;; -------------------------
;; Views
;; state management


(defonce state (initial-state))

;; (defn toggle-class [obj j]
;;   (let [s (:text obj)]
;;     (if (is-uppercase s)
;;       (swap! enteries assoc j {:text (str/lower-case s) :is-completed true})
;;       (swap! enteries assoc j {:text (str/upper-case s) :is-completed true}))))
;; (defn lister []
;;   (let [item
;;         [:div (map (fn[i,j][:div.abc {:key j :style {:color (if (:is-completed i) "green" "blue")} :on-click #(toggle-class (nth @enteries j) j)} (:text i)]) @enteries (range @nums))]] item))

(defn calender []
  [table-container state])

(defn main-page []
  (reagent/create-class
    {:component-did-mount #()
     :reagent-render
       (fn[]
         [:div
           [title (aget TITLE @(:current-month state))]
           [form-container state]
           [:br]
           [calender]])}))

(defn home-page []
  [:div [:h2 "Welcome to calender"]
   [:div [:a {:href "/about"} "go to about page"]]
   [:div [:a {:href "/main"} "go to my calender app"]]])

(defn about-page []
  [:div [:h2 "About calender"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/main" []
  (session/put! :current-page #'main-page))


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
