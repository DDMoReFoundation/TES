# [AUTHOR] hnyberg
# [DATE] 2013/05/03 14:22:22
# [DESCRIPTION] Mangoesque histogram
# [KEYWORDS]  

PHENO <- read.table("../Data/PHENO.dta", header = T)

hist(PHENO$WT, col="orange", main="Weight histogram", xlab=" ", sub="In awesome Mango colour", labels=FALSE)