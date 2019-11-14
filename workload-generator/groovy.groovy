for (number in 2..9) {
    def request_delay = vars.get("request" + number + "_delay").toInteger()
    if (request_delay != -1) {
        vars.put("if_request" + number, "true")
    }
    vars.put("request" + number + "_delay", (request_delay * 1000).toString())
}

def request_num = vars.get("request_num").toInteger()
def query1 = vars.get("query1").toString()
def query2 = vars.get("query2").toString()

setRequestVar(1, Math.floor((request_num + 1) / 2).toInteger(), request_num, query1, query2)

def setQuery(int requestNumStart, int requestNumEnd, String query) {
    for (number in requestNumStart..requestNumEnd) {
        vars.put("request" + number + "_query", query)
    }
}

def setPage(int requestNumStart, int requestNumEnd, int startPage) {
    for (number in requestNumStart..requestNumEnd) {
        vars.put("request" + number + "_page", startPage.toString())
        startPage++
    }
}

def setPageAccordingToQuery(int requestNumStart, int requestNumEnd, String query1, String query2) {
    if (query1 == query2) {
        setPage(requestNumStart, requestNumEnd, requestNumStart)
    } else {
        setPage(requestNumStart, requestNumEnd, 1)
    }
}

def setRequestVar(int requestNumStart, int requestNumMedian, int requestNumEnd, String query1, String query2) {
    setQuery(requestNumStart, requestNumMedian, query1)
    setQuery(requestNumMedian + 1, requestNumEnd, query2)
    setPage(requestNumStart, requestNumMedian, requestNumStart)
    setPageAccordingToQuery(requestNumMedian + 1, requestNumEnd, query1, query2)
}
