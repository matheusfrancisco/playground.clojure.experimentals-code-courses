; Programing to Abstractions

(defn titleize
  [topic]
  (str topic " for the Brave and True"))

(map titleize ["Matheus" "Ragnarok"])
; => ("Matheus for the Brave and True" "Ragnarokfor the Brave and True")
(map titleize #{"Matheus" "Ragnarok"})
; => ("Matheus for the Brave and True" "Ragnarok for the Brave and True")
(map titleize '("Matheus" "Ragnarok"))

;=> ("Matheus for the Brave and True" "Ragnarok for the Brave and True")
(map #(titleize (second %)) {:unconmfortable-thing "Matheus"})

;=> ("Matheus for the Brave and True" "Ragnarok for the Brave and True")


; Abstraction Through Indirection

; map, first, rest, cons -- it calls the seq function

(seq '(1 2 3))
;=> (1 2 3)
(seq [1 2 3])
;=> (1 2 3)
(seq #{1 2 3})
;=> (1 2 3)
(seq {:name "Matheus" :occupation "Dev"})
;=>([:name "Matheus"] [:occupation "Dev"])

(into {} (seq {:a 1 :b 2 :c 3}))
; => {:a 1, :c 3, :b 2}

;As ong as data structure implements the sequence abstractions, it can use the extensive seq library, which includes such
;superstar functions as reduce, filter,distinct, group-by, and dozens more.


;map
(map inc [1 2 3])
; => (2 3 4)

(map str ["a" "b" "c"] ["A" "B" "C"])
; => ("aA" "bB" "cC")

;when you pass map multiple collections, the elements of the first
;collection (["a" "b" "c"]) will be passed as the first argument of the mapping
;function the elements of the second collection (["A" "B"]) will be passed as the second
; argument, and so on. Just be sure that your mapping functoin can take a number of arguments equal to
; the number of collections you're passing to map


(def human-consumption [8.1 7.3 6.6 5.0])
(def critter-consumption [0.0 0.2 0.3 1.1])
(defn unify-diet-data
  [human critter]
  {:human human
   :critter critter})
(map unify-diet-data human-consumption critter-consumption)
; => ({:human 8.1, :critter 0.0}
;{:human 7.3, :critter 0.2}
;{:human 6.6, :critter 0.3}
;{:human 5.0, :critter 1.8})


(def sum #(reduce + %))
(def avg #(/ (sum %) (count %)))
(defn stats
  [numbers]
  (map #(% numbers) [sum count avg]))

(stats [3 4 10])
; stats function interates over a vector of functions, applying each functoin to numbers.

;Additionally, Clojurists often use map to retrieve the value associated
;with a keyword from a collection of map data structures. Because keywords
;can be used as functions, you can do this succinctly. Here’s an example:

(def identities
  [{:alias "Batman" :real "Bruce Wayne"}
   {:alias "Spider-Man" :real "Peter Parker"}
   {:alias "Santa" :real "Your mom"}
   {:alias "Easter Bunny" :real "Your dad"}])

(map :real identities)
; => ("Bruce Wayne" "Peter Parker" "Your mom" "Your dad")


;Reduce

(reduce (fn [new-map [key val]]
          (assoc new-map key (inc val)))
          {}
          {:max 30 :min 10})
; => {:max 31, :min 11}

;In this example, reduce treats the argument {:max 30 :min 10} as a
;sequence of vectors, like ([:max 30] [:min 10]). Then, it starts with an
;empty map (the second argument) and builds it up using the first argument,
;an anonymous function. It’s as if reduce does this:

(assoc (assoc {} :max (inc 30))
       :min (inc 10))


(reduce (fn [new-map [key val]]
          (if (> val 4)
            (assoc new-map key val)
            new-map))
        {}
        {:human 4.1 :critter 3.9})
; => {:human 4.1}


;The takeaway here is that reduce is a more flexible function than it first
;appears. Whenever you want to derive a new value from a seqable data
;structure, reduce will usually be able to do what you need. If you want an
;exercise that will really blow your hair back, try implementing map using
;reduce, and then do the same for filter and some after you read about
;them later in this chapter


;Take, drop, take-while, and drop-while

(take 3 [1 2 3 4 5 6 7 8 9 10])
; => (1 2 3)

(drop 3 [1 2 3 4 5 6 7 8 9 10])
; => (4 5 6 7 8 9 10)


;Their cousins take-while and drop-while are a bit more interesting.
;Each takes a predicate function (a function whose return value is evaluated
;for truth or falsity) to determine when it should stop taking or dropping.
;Suppose, for example, that you had a vector representing entries in your
;“food” journal. Each entry has the year, month, day, and what you ate. To
;preserve space, we’ll only include a few entries:

(def food-journal
   [ {:month 1 :day 1 :human 5.3 :critter 2.3}
    {:month 1 :day 2 :human 5.1 :critter 2.0}
    {:month 2 :day 1 :human 4.9 :critter 2.1}
    {:month 2 :day 2 :human 5.0 :critter 2.5}
    {:month 3 :day 1 :human 4.2 :critter 3.3}
    {:month 3 :day 2 :human 4.0 :critter 3.8}
    {:month 4 :day 1 :human 3.7 :critter 3.9}
    {:month 4 :day 2 :human 3.7 :critter 3.6}])

;This example uses the anonymous function #(< (:month %) 3) to test
;whether the journal entry’s month is out of range:

;When take-while reaches the first March entry, the anonymous
;function returns false, and take-while returns a sequence of every
;element it tested until that point


(take-while #(< (:month %) 3) food-journal)
;=>({:month 1, :day 1, :human 5.3, :critter 2.3}
; {:month 1, :day 2, :human 5.1, :critter 2.0}
; {:month 2, :day 1, :human 4.9, :critter 2.1}
; {:month 2, :day 2, :human 5.0, :critter 2.5})


;the same to drop-while

(drop-while #(< (:month %) 3) food-journal)

; =>({:month 3, :day 1, :human 4.2, :critter 3.3}
; {:month 3, :day 2, :human 4.0, :critter 3.8}
; {:month 4, :day 1, :human 3.7, :critter 3.9}
; {:month 4, :day 2, :human 3.7, :critter 3.6})
;

(take-while #(< (:month %) 4) (drop-while #(< (:month %) 2) food-journal))
; by using take-while and drop while together, you can get data for just feb and march:
;({:month 2, :day 1, :human 4.9, :critter 2.1}
; {:month 2, :day 2, :human 5.0, :critter 2.5}
; {:month 3, :day 1, :human 4.2, :critter 3.3}
; {:month 3, :day 2, :human 4.0, :critter 3.8})

;=>This example uses drop-while to get rid of the January entries, and
;then it uses take-while on the result to keep taking entries until it reaches
;the first April entry
;

;filter and some
;Using filter to return all elements of a sequence that test true for a predicate function. Here
; are the juornal entries where human consumption is less than five liters

(filter #(< (:human %) 5) food-journal)
;({:month 2, :day 1, :human 4.9, :critter 2.1}
; {:month 3, :day 1, :human 4.2, :critter 3.3}
; {:month 3, :day 2, :human 4.0, :critter 3.8}
; {:month 4, :day 1, :human 3.7, :critter 3.9}
; {:month 4, :day 2, :human 3.7, :critter 3.6})

(filter #(< (:month %) 3) food-journal)
;this use is perfectly fine, but filter can end up processing all of your
;data,which isn't always necessary.

;Some
; The some function does that, returning the
;first truthy value (any value that’s not false or nil) returned by a predicate 
;function:

(some #(> (:critter %) 5) food-journal)
; => nil

(some #(> (:critter %) 3) food-journal)
; => true


;Sort and sort-by
(sort [3 5 4])
; =>  (3 4 5)

(sort-by count ["aaa" "c" "bb"])
;=> ("c" "bb" "aaa")

;concat
(concat [1 2][3 4])
;=>(1 2 3 4)


; Lazy seqs
; Many functions, incluiding map and filter, return a lazy seq. A lazy seq is a seq whose
; members aren't computed until you try to acess them. Computing a seq's members is called realizing the seq.

; Demonstring Lazyy Seq Efficient

(def vampire-database
  {0 {:makes-blood-puns? false, :has-pulse? true :name "McFishwich"}
  1 {:makes-blood-puns? false, :has-pulse? true :name "McMackson"}
  2 {:makes-blood-puns? true, :has-pulse? false :name "Damon Salvatore"}
  3 {:makes-blood-puns? true, :has-pulse? true :name "Mickey Mouse"}})

;
(defn vampire-related-details
  [social-security-number]
  (Thread/sleep 1000)
  (get vampire-database social-security-number))

(defn vampire?
  [record]
  (and (:makes-blood-puns? record)
       (not (:has-pulse? Record))
        record))

(defn identify-vampire
  [social-security-number]
  (first (filter vampire?
                 (map vampire-related-details social-security-number))))

(time (vampire-related-details 0))
; => "Elapsed time: 1001.042 msecs"
; => {:name "McFishwich", :makes-blood-puns? false, :haspulse? true}

;You have a function, vampire-related-details, which takes one
;second to look up an entry from the database. Next, you have a function,
;vampire?, which returns a record if it passes the vampire test; otherwise, it
;returns false. Finally, identify-vampire maps Social Security numbers
;to database records and then returns the first record that indicates
;vampirism.


; Infinite Sequences

(concat (take 8 (repeat "na")) ["Batman!"])
; => ("na" "na" "na" "na" "na" "na" "na" "na" "Batman!")


;You can also use repeatedly, which will call the provided function to
;generate each element in the sequence:

(take 3 (repeatedly (fn [] (rand-int 10))))

;Here, the lazy sequence returned by repeatedly generates every new
;element by calling the anonymous function (fn [] (rand-int 10)),
;which returns a random integer between 0 and 9. If you run this in your
;REPL, your result will most likely be different from this one


;A lazy seq’s recipe doesn’t have to specify an endpoint. Functions like
;first and take, which realize the lazy seq, have no way of knowing what
;will come next in a seq, and if the seq keeps providing elements, well,
;they’ll just keep taking them. You can see this if you construct your own
;infinite sequence:

defn (even-numbers
  ([] (even-numbers 0))
  ([n] (cons n (lazy-seq (even-numbers (+ n 2))))))

(take 10 (even-numbers))
; => (0 2 4 6 8 10 12 14 16 18)

;cons returns a new list with an element appended to


; The Collections Abstraction
; verctors, maps, lists, and sets
;count, empty?, every?

