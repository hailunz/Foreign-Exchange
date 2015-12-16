import csv

from sklearn.feature_extraction.text import TfidfVectorizer, CountVectorizer
from sklearn.decomposition import NMF
from collections import defaultdict
from bs4 import BeautifulSoup, NavigableString

from nltk.stem.snowball import SnowballStemmer
from nltk.corpus import stopwords
from nltk.tokenize import wordpunct_tokenize
import nltk
from soupselect import select
import operator
import json

sessions = {}
stemmer = SnowballStemmer("english")
lemmatizer = nltk.WordNetLemmatizer()
stop_words = set(stopwords.words('english'))

with open("data/sessions.csv", "r") as sessions_file:
    reader = csv.reader(sessions_file, delimiter = ",")
    reader.next() # header
    for row in reader:
        session_id = int(row[0])
        filename = row[1]
        page = open(filename).read()
        soup = BeautifulSoup(page)
        body = select(soup, "body p")

        if body:
            content = ""
            for c in body:
                content += " " + c.text

            body_content = ' '.join([lemmatizer.lemmatize(w) for w in wordpunct_tokenize(content)
                                     if w.lower() not in stop_words and len(lemmatizer.lemmatize(w))>2
                                     and lemmatizer.lemmatize(w).isalpha()])

            #print body_content
            sessions[session_id] = {"body" : body_content, "title": filename }


corpus = []
titles = []
for id, session in sorted(sessions.iteritems(), key=lambda t: int(t[0])):
    corpus.append(session["body"])
    titles.append(session["title"])

#vectorizer = TfidfVectorizer(analyzer='word', ngram_range=(1,1), min_df = 0, stop_words = 'english')
vectorizer = CountVectorizer(analyzer='word', ngram_range=(1,1), min_df = 0, stop_words = 'english')
matrix =  vectorizer.fit_transform(corpus)
feature_names = vectorizer.get_feature_names()

import lda
import numpy as np

vocab = feature_names

n_topics = 10

model = lda.LDA(n_topics=n_topics, n_iter=500, random_state=1)
model.fit(matrix)

topic_word = model.topic_word_
n_top_words = 10

for i, topic_dist in enumerate(topic_word):
    topic_words = np.array(vocab)[np.argsort(topic_dist)][:-n_top_words:-1]
    print('Topic {}: {}'.format(i, u' '.join(topic_words).encode('utf-8')))

doc_topic = model.doc_topic_
for i in range(0, len(titles)):
    print("{} (top topic: {})".format(titles[i], doc_topic[i].argmax()))
    print(doc_topic[i].argsort()[::-1][:3])

with open("data/topics.csv", "w") as file:
    writer = csv.writer(file, delimiter=",")
    writer.writerow(["topicId", "word"])

    for i, topic_dist in enumerate(topic_word):
        topic_words = np.array(vocab)[np.argsort(topic_dist)][:-n_top_words:-1]
        for topic_word in topic_words:
            writer.writerow([i, topic_word.encode('utf-8')])


with open("data/sessions-topics.csv", "w") as file:
    writer = csv.writer(file, delimiter=",")
    writer.writerow(["sessionId", "topicId"])

    doc_topic = model.doc_topic_
    for i in range(0, len(titles)):
        writer.writerow([i, doc_topic[i].argmax()])
        print("{} (top topic: {})".format(titles[i], doc_topic[i].argmax()))
        print(doc_topic[i].argsort()[::-1][:3])


with open("data/topic-matrix.csv", "w") as file:
    writer = csv.writer(file, delimiter=",")
    writer.writerow([w.encode("utf-8") for w in feature_names])
    for j in range(0,len(model.topic_word_)):
        writer.writerow(model.topic_word_[j])



count_docs = [0] * n_topics
doc_topic = model.doc_topic_
for i in range(0, len(titles)):
    index = doc_topic[i].argmax()
    count_docs[index] +=1

print count_docs

total_sum = sum(count_docs)
child = []

topic_word = model.topic_word_
for i, topic_dist in enumerate(topic_word):
    topic_words = np.array(vocab)[np.argsort(topic_dist)][:-n_top_words:-1]
    words_score = np.array(topic_dist)[np.argsort(topic_dist)][:-n_top_words:-1]
    data = dict()
    data["name"] = "Topic:" + str(i)
    data["size"] = count_docs[i]
    children_tmp = []
    sum_scores = sum(words_score)
    for j in range(0, n_top_words-1):
        topic = dict()
        topic["name"] = topic_words[j]
        topic["size"] = words_score[j]/sum_scores * count_docs[i]/total_sum
        topic["group"] = i
        children_tmp.append(topic)

    data["children"] = children_tmp
    child.append(data)

with open("result/topics.json", "w") as outfile:
    json.dump({"name":"Topics", "children":child}, outfile, indent=4)

