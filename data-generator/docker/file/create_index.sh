EXCELLENT_SERVER_ENTRYPOINT=http://${excellent_item_host}:${excellent_item_port}
GOOD_SERVER_ENTRYPOINT=http://${good_item_host}:${good_item_port}
BAD_SERVER_ENTRYPOINT=http://${bad_item_host}:${bad_item_port}
RANKING_ENTRYPOINT=http://${ranking_host}:${ranking_port}
SUMMARY_ENTRYPOINT=http://${summary_host}:${summary_port}

curl -XDELETE "${EXCELLENT_SERVER_ENTRYPOINT}/excellent_items"
echo
curl -XPUT "${EXCELLENT_SERVER_ENTRYPOINT}/excellent_items" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_doc": {
      "dynamic": "strict",
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "ik_max_word"
        },
        "related_items": {
          "type": "keyword"
        }
      }
    }
  }
}'
echo "${EXCELLENT_SERVER_ENTRYPOINT}/excellent_items created!"

curl -XDELETE "${GOOD_SERVER_ENTRYPOINT}/good_items"
echo
curl -XPUT "${GOOD_SERVER_ENTRYPOINT}/good_items" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_doc": {
      "dynamic": "strict",
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "ik_max_word"
        },
        "related_items": {
          "type": "keyword"
        }
      }
    }
  }
}'
echo "${GOOD_SERVER_ENTRYPOINT}/good_items created!"

curl -XDELETE "${BAD_SERVER_ENTRYPOINT}/bad_items"
echo
curl -XPUT "${BAD_SERVER_ENTRYPOINT}/bad_items" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_doc": {
      "dynamic": "strict",
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "ik_max_word"
        },
        "related_items": {
          "type": "keyword"
        }
      }
    }
  }
}'
echo "${BAD_SERVER_ENTRYPOINT}/bad_items created!"

curl -XDELETE "${RANKING_ENTRYPOINT}/items_ranking"
echo
curl -XPUT "${RANKING_ENTRYPOINT}/items_ranking" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_doc": {
      "dynamic": "strict",
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "ik_max_word"
        },
        "price": {
          "type": "double"
        },
        "ratesum": {
          "type": "integer"
        },
        "category": {
          "type": "keyword"
        }
      }
    }
  }
}'
echo "${RANKING_ENTRYPOINT}/items_ranking created!"

curl -XDELETE "${SUMMARY_ENTRYPOINT}/items_summary"
echo
curl -XPUT "${SUMMARY_ENTRYPOINT}/items_summary" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_doc": {
      "dynamic": "strict",
      "properties": {
        "title": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "price": {
          "type": "double",
          "doc_values": false,
          "index": false
        },
        "ratesum": {
          "type": "integer",
          "doc_values": false,
          "index": false
        },
        "category": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "related_items": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "a": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "b": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "c": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "d": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "e": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "f": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "g": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "h": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "i": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "j": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "k": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "l": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "m": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "n": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "o": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "p": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "q": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "r": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "s": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "t": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "u": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "v": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "w": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "x": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "y": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        },
        "z": {
          "type": "keyword",
          "doc_values": false,
          "index": false
        }
      }
    }
  }
}'
echo "${SUMMARY_ENTRYPOINT}/items_summary created"
