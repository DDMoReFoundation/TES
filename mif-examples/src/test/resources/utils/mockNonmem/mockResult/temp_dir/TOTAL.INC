!*********************************COPYRIGHT******************************************
!										    !
!       THE NONMEM SYSTEM MAY BE DISTRIBUTED ONLY BY ICON DEVELOPMENT               !
!       SOLUTIONS.                                                                  !
!                                                                                   !
!       COPYRIGHT BY ICON DEVELOPMENT SOLUTIONS                                     !
!       2009-2011 ALL RIGHTS RESERVED.                                              !
!                                                                                   !
!       DO NOT ATTEMPT TO MODIFY CODE WITHOUT FIRST CONSULTING WITH                 !
!       ICON DEVELOPMENT SOLUTIONS.                                                 !
!                                                                                   !
!************************************************************************************
!
!-----------------------------HISTORY------------------------------------------------
! VERSION     : NONMEM VII
! AUTHOR      : ROBERT J. BAUER
! CREATED ON  : ....
! LANGUAGE    : FORTRAN 90/95
! LAST UPDATE : FEB/2009 - BAYESIAN METHOD INCLUDED
!               JUN/2009 - INTEGRATED BETA 5.5B4 MODIFICATIONS
!
!----------------------------- TOTAL.INC -------------------------------------
!
! DESCRIPTION : 
!
! ARGUMENTS   : NONE
!
! CALLED BY   : BAYES_HOOKS,SAEM_HOOKS
!
! CALLS       : NONE
!
! ALGORITHM   : 
!
! MODULES USED: NONE
!
! CONTAINS    : NONE
!
! LOCAL'S     : NONE
!
!---------------------------- END OF HEADER -----------------------------------------
!
! BAYES_HOOKS.INC file inserted here

      USE SIZES,     ONLY: DPSIZE,ISIZE,MAXVRESWRES,MAXVRESN                   !7.2 
                           !5.5B4, LVR,LTH,LPAR3,PC,PG,LNP4,SD,OWRB,OWRE,MXNAME
      USE FSIZES,    ONLY: F_MAXOMEG,F_MAXPTHETA,F_LPAR3,F_MAXNSP,F_LVR,F_LPAR !7.2
!      
      USE NMDATA,    ONLY: IUN,TITL01,NSIGL=>NSIG
! INTEGER      
      USE NMPRD_INT, ONLY: NTHETA,NETA,NEPS,IVEST,IVESTB,NAETA,LVOUT,ISETNTH,  &
                           NOEPS,IFRSTDER,ISECDER,INDXS,IPROB,IQUIT,IDXMDV,NDI
      USE CMNM1_INT, ONLY: NIND,NMAXMIX,ITRANPAR,ISCALPAR,IND,IOMGR,IFRSTGR,   &
                           MAXFN,NSIG,IMSFO,IPRNTINT,IREPEAT1,IPOSTHOC,IINTER, &
                           IINTER2,ILAP,INONP,ICOND,INPETA,INPMSFO,ISLOWGR,    &
                           ICENTER,INUMER,IFOMOD,IPRDFLG,IHYBRID,ISTIELT,IMIX, &
                           IREPEAT2,IETABAR,SIGL,NPAR10,IPRIOR,VRES_OFFSET,    &
                           IXTBLE_OFFSET,WRES_DEFAULT,SIGLCOV,TOLCOV,VRESD,    &
                           VRESN,SIGLO,SIGLOCOV,ATOLCOV !7.2B55A
      USE CMNM2_INT, ONLY: NAETAI,LVOUTI,NAETAN,LVOUTN,NTHT_P,NETA_P,NEPS_P,   &
                           NEXP_P,ICOMPRS,IRECIDX,NRECIND,ICOVR,IESTM,NESTETAH,&
                           IEOB21,IEOB22,NINDETA,NWINTL,NOBSIND,IEVAL !7.1.1
      USE CMNM3_INT, ONLY: AVAIL,INPRECOM,ICOVMAT,ICOVEIGN,ICOVPRNT
      USE CMNM4_INT, ONLY: IINV,IABRTEST,IER,NOHABORT !7.2
      USE CMNM5_INT, ONLY: NETAEST,NR,NS
      USE CMNM6_INT, ONLY: NTABLES2,NSCAT2,NFNCEVAL,NETASCL,IWREST,IESTEXCL,   & 
                           IESTEXCL,ICOVEXCL,NITEM,IXTBLE,IDXABS,IDXORD
      USE CMNM7_INT, ONLY: NREPSLFT,ISORTOBJ,NEIGNVAL,ITBL
      USE ROCM_INT,  ONLY: MIXCALL,IEST_ERR,ICOV_ERR
      USE NMBAYES_INT
! REAL                           
      USE NMPRD_REAL,ONLY: DB,LB,UB,INFNTY
      USE CMNM1_REAL,ONLY: VDATREC,UCPAR10
      USE CMNM2_REAL,ONLY: THETAO,VEST,ETAIND,VETA,VINVF
      USE CMNM3_REAL,ONLY: THETA,VARNC,VINV,LGDET,LGDETB
      USE CMNM4_REAL,ONLY: OFVOLD
      USE CMNM5_REAL,ONLY: UCPETA,VINVETA,LGDETETA,R0,R11
      USE CMNM6_REAL,ONLY: SCLETA
      USE CMNM7_REAL,ONLY: LGDETP,COVM,SE_THTA,SE_OMSG,CORRM,EIGENVLS,COVINM,  &
                           SMATV,RMATV
      USE ROCM_REAL, ONLY: HL1,POSTV,THETAC,VARNF,MIXP,OBJECT,THETAF,OMEGAF,   &
                           SIGMAF,SETH,SEOM,SESIG
      USE CMNM2_REAL,ONLY: ETABAR,SUMETASQ,SHRINKETA,SHRINKEPS !7.2
      USE NMBAYES_REAL
! CHARACTER
      USE ROCM_CHAR, ONLY: LABEL
      USE NMBAYES_CHAR
!
      IMPLICIT NONE