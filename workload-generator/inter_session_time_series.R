options(max.print = .Machine$integer.max)

library("forecast")

session_num <- read.csv(file = '../../Dataset/session_num_per_second_in_one_month_600.csv', header = FALSE)
session_num_per_second <- tail(session_num$V2, - 86400)
session_ts <- msts(session_num_per_second, seasonal.periods = c(86400, 604800))
session_ts_model <- mstl(session_ts)
print(session_ts_model)
