(ns dynamo.util)

(defn map-values [f m]
  (->> m
       (map (fn [[k v]] [k (f v)]))
       (into {})))

(defn map-leaves
  "Applies f to every leaf of m.
  Assumes all vals are either seqs or maps."
  [f m]
  (->> m
       (map (fn [[k v]]
              (if (seq? v)
                [k (map f v)]
                [k (map-leaves f v)])))
       (into {})))
