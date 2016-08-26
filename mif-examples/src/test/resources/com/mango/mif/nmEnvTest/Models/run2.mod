;; Based on: run1.mod
$PROBLEM    PHENOBARBITAL VWT covariate model
$INPUT      ID TIME AMT WT APGR DV
$DATA       ../Data/PHENO.dta IGNORE=@
$SUBROUTINE ADVAN1 TRANS2

$PK

  VWT = ( 1 + THETA(4)*(WT - 1.3))   ; Effect of weight on V

  TVCL = THETA(1)                    ; Population value of CL
  TVV  = THETA(2)*VWT                ; Population value of V with WT effect
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

$THETA  (0,.05)                      ; 1. TVCL 
$THETA  (0,1.45)                     ; 2. TVV  
$THETA  (0, 0.5)                     ; 3. Additive error
$THETA  (0, 0.01)                    ; 4. VWT

$OMEGA  0.228                        ; 1. CL  ; variance for ETA(1)
$OMEGA  0.146                        ; 2. V   ; variance for ETA(2)

$SIGMA  1 FIX                        ; Residual error

$ESTIMATION METHOD=1 MAXEVAL=9999 NOABORT PRINT=1  ; FOCE

$TABLE      ID TIME DV PRED IPRED IRES IWRES WRES MDV CL V ETA1 ETA2 APGR WT CWRES NOPRINT
            NOAPPEND ONEHEADER FILE=run2.tab

