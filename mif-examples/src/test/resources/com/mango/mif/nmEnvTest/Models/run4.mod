;; Based on: run3.mod
$PROBLEM    PHENOBARBITAL VWT and CLWT covariate model
$INPUT      ID TIME AMT WT APGR DV
$DATA       ../Data/PHENO.dta IGNORE=@
$SUBROUTINE ADVAN1 TRANS2

$PK

  VWT = ( 1 + THETA(4)*(WT - 1.3))   ; Effect of weight on V
  CLWT = ( 1 + THETA(5)*(WT - 1.3))  ; Effect of weight on CL

  TVCL = THETA(1)*CLWT               ; Population value of CL with WT effect
  TVV  = THETA(2) *VWT               ; Population value of V with WT effect
  CL   = TVCL*EXP(ETA(1))            ; Individual value of CL
  V    = TVV*EXP(ETA(2))             ; Individual value of V
  S1   = V

$ERROR

  IPRED  = F                         ; Individual prediction
  IRES   = DV - F                    ; Individual residual
  W      = THETA(3)                  ; Additive residual error
  IF(W.EQ.0) W = 1
  IWRES  = IRES/W                    ; Individual weighed residual
  Y      = IPRED + W*EPS(1)

$THETA  (0,.005)                     ; 1. TVCL
$THETA  (0,1.3)                      ; 2. TVV
$THETA  (0,2.5)                      ; 3. Additive error
$THETA  (0,0.6)                      ; 4. VWT
$THETA  (0,0.8)                      ; 5. CLWT

$OMEGA  0.228                        ; 1. CL  ; variance for ETA(1)
$OMEGA  0.146                        ; 2. V   ; variance for ETA(2)

$SIGMA  1 FIX                        ; Residual error

$ESTIMATION METHOD=1 MAXEVAL=9999 NOABORT PRINT=1  ; FOCE
$ESTIMATION METHOD=IMPMAP PRINT=1 
$ESTIMATION METHOD=ITS PRINT=1 



$TABLE      ID TIME DV PRED IPRED IRES IWRES WRES MDV CL V ETA1 ETA2 APGR WT CWRES NOPRINT
            NOAPPEND ONEHEADER FILE=run4.tab

