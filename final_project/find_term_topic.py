__author__ = 'hailunzhu'

import csv

from sklearn.feature_extraction.text import TfidfVectorizer, CountVectorizer
from sklearn.decomposition import NMF
from collections import defaultdict
from bs4 import BeautifulSoup, NavigableString

from nltk.stem.snowball import SnowballStemmer
from nltk.corpus import stopwords
from nltk.tokenize import wordpunct_tokenize
import nltk
import operator
import numpy as np
import json

stemmer = SnowballStemmer("english")
lemmatizer = nltk.WordNetLemmatizer()
stop_words = set(stopwords.words('english'))
n_top_words = 10

feature_names = []
matrix = []

count = 0
with open("data/topic-matrix.csv", "r") as model_file:
    reader = csv.reader(model_file, delimiter = ",")
    for row in reader:
        if count == 0:
            feature_names = row
            count += 1
        else:
            tmp = [float(cell) for cell in row]
            matrix.append(tmp)
            count += 1
print count
vocab = feature_names
n_topics = len(matrix)

print n_topics

while(True):
    term = raw_input("User input term:")
    origin_term = term
    if term == "":
        print "EXIT"
        break

    term = term.strip()

    terms = term.split(" ")
    scores = dict()
    for i in range(0,n_topics):
        scores[i] = 0

    for term in terms:
        term = lemmatizer.lemmatize(term.lower())

        if term in stop_words or len(term) < 2:
            continue

        if term not in feature_names:
            continue

        i = feature_names.index(term)
        print term
        print i

        for j in range(0,len(matrix)):
            scores[j] += matrix[j][i]

    sorted_scores = sorted(scores.items(), key=operator.itemgetter(1),reverse=True)
    labels = []
    values = []
    child = []

    total_sum = 0
    for index in range(0,5):
        (k,v) = sorted_scores[index]
        total_sum += v

    for index in range(0,5):
        (k,v) = sorted_scores[index]
        print k,v
        topic_words = np.array(vocab)[np.argsort(matrix[k])][:-n_top_words:-1]
        words_score = np.array(matrix[k])[np.argsort(matrix[k])][:-n_top_words:-1]

        topic_name = u' '.join(topic_words).encode('utf-8')
        labels.append(topic_name)
        values.append(v)
        data = dict()
        data["name"] = "Topic:" + str(k)
        data["size"] = v
        children_tmp = []

        sum_scores = 0
        for j in range(0,n_top_words-1):
            sum_scores += words_score[j]

        for j in range(0,n_top_words-1):
            topic = dict()
            topic["name"] = topic_words[j]
            topic["size"] = words_score[j]/sum_scores * v/total_sum
            topic["group"] = k
            children_tmp.append(topic)

        data["children"] = children_tmp
        child.append(data)

        print "Match topic:" + 'Topic {}: {}'.format(k, u' '.join(topic_words).encode('utf-8'))

    print labels
    print values

    # import plotly.plotly as py
    # import plotly; plotly.tools.set_credentials_file(username='hailunzhu03', api_key='e76rcdaas1')
    #
    #
    # fig = {
    #     'data': [{'labels': labels,
    #               'values': values,
    #               'type': 'pie'}],
    #     'layout': {'title': 'User term: ' + origin_term}
    # }
    #
    # url = py.plot(fig, filename='term-'+origin_term)


    with open("result/term.json", "w") as outfile:
        json.dump({"name":origin_term, "children":child}, outfile, indent=4)