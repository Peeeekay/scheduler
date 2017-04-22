(ns calender.handler
  (:require [clojure.walk]
            [calender.db :as db]
            [calender.views :as views]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [calender.middleware :refer [wrap-middleware]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn json->map [json]
  (clojure.walk/keywordize-keys json))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn test-function []
  (let [data {:date 2 :data "data" :month "JAN" :start-time 1 :end-time 2 :duration 1}]
    (db/create-date-node data)
  (response {:text (format "The test route works: %s" (:name data))})))

(defn handle-send-data [req]
  (let [input (json->map req)]))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  (GET "/test" [] (test-function))
;;(POST "/test" req (test-function req))
  (resources "/")
  (not-found "Not Found"))

(def app (-> #'routes
            (wrap-json-body)
            (wrap-json-response)))
