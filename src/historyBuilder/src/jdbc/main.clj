(ns jdbc.main
(:require [clojure.java.jdbc :as j])
(:require [clojure.java.io :as io])
)

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/umacopy?useSSL=false"
               :user "root" 
               })

(defn toCsv [histories]
  (with-open [w (clojure.java.io/writer  "../../out/horseRaceHistory.csv" :append true :create true :truncate true)]
    (doseq [{:keys [horse-id race-id before-race-id]} histories]
      (.write w (str horse-id "," race-id "," before-race-id "\n")))))

(defn create-race-history [horse-races]
  (->> (reduce
        #(conj %1 {:horse-id (:horse-id %2) :race-id (:race-id %2) :before-race-id (:race-id (last %1))})
        []
        horse-races)
       (drop 1)))

(defn get-horse-ids []
  (->> (j/query mysql-db ["SELECT DISTINCT horse_id FROM raceResult"])
  (map :horse_id)))

(defn get-horse-races [horse-id]
  (j/query mysql-db [(str "SELECT rr.horse_id as `horse-id`, r.id as `race-id` 
			FROM raceResult rr
				INNER JOIN race r ON r.id = rr.race_id
			WHERE rr.horse_id = '" horse-id "'
			ORDER BY r.date")]))

(defn build-history-by-horse-id [horse-id]
  (let [horse-races (get-horse-races horse-id)]
    (when (< 1 (count horse-races))
      (-> (create-race-history horse-races)
          (toCsv)))))

(defn -main [& args]
  (println "Start build race histories.")
  (doseq [horse-id (get-horse-ids)] (build-history-by-horse-id horse-id))
  (println "done."))