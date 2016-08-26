!*********************************COPYRIGHT******************************************
!									                                        	    !
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
! AUTHOR      : NOUS INFOSYSTEMS, AND ICON DEVELOPMENT SOLUTIONS
! CREATED ON  : MAY 30, 2008
! LANGUAGE    : FORTRAN 90/95
! LAST UPDATE : JUL/2008 - CREATED MODULES TO REPLACE ALL COMMON BLOCKS IN NONMEM VII
!               FEB/2009 - CREATED MODULES TO REPLACE BAYES MODEL COMMON BLOCKS
!               JUN/2009 - INTEGRATED BETA 5.5B4 MODIFICATIONS
!               FEB/2011 - INTEGRATED 7.2BETA5.8B MODIFICATIONS
!
!---------------------------- PRGLOBALP.F90  -----------------------------------------
!
! DESCRIPTION   : These global variables are allocated dynamically during the run time
!                 based on the prsizes.f90 (created during run time) parameters 
!                 All GLOBAL variables are grouped under different modules. Each module 
!                 is sub-divided based on the data type (Integer, Real, character and 
!                 Logical) and data type is suffixed to each module, Ex: PRCM_INT 
!                 represents module containing only integer variables.   
!
! MODULE PRMOD  : Grouped following COMMONS: 
!                 PRBI,PRDDE1,PRMON3,PRMONO,PRNAME,PROUT,PRDPK2,PRDPK3,PRDPK4
!
! MODULE PRGEAR : Grouped following COMMONS: 
!                 PRBAND,PRGEAR
!
! MODULE PRLS01 : Grouped following COMMONS: 
!                 PRLS01
!
! MODULE PRCM   : Grouped following COMMONS: 
!                 PRCM00,PRCM01,PRCM02,PRCM03,PRCM11,PRCM12,PRCM6A,PRCM7,PRCM7L,
!                 PRCMDC,PRCMDE,PRCMET,PRCMLS,PRCMLX,,PRCMW,PRCMX1,PRCMX2
!
! MODULE PRCOM  : Grouped following COMMONS: 
!                 PRCOM0,PRCOM1,PRCOM2,PRCOM3,PRCOM4,PRCOM5,PRCOM6,PRCOM7,PRCOMA,
!                 PRCOMC,PRCOMD,PRCOME,PRCOMF,PRCOMG,PRCOMI,PRCOMJ,PRCOML,PRCOMM,
!                 PRCOMN,PRCOMP,PRCOMS,PRCOMT,PRCOMU,PRCOMW,PRCOMX
!
! MODULE PROCM  : Grouped following COMMONS: 
!                 PROCM1,PROCM2,PROCM3,PROCM4,PROCM5,PROCM6,PROCM7,PROCM8,PROCM9,
!                 PROCMA,PROCMB,PROCMC
!
! MODULE TLCOM  : Grouped following COMMONS: 
!                 PRINFN, PRDPK1
!
! MODULE PR_BAYES_INT  : Grouped following COMMONS: 
!                        DLS001, DLSA01, SADVAN2_COMMON2
!
! MODULE PR_BAYES_REAL  : Grouped following COMMONS: 
!                         DLS001, DLSA01, SADVAN2_COMMON2
!
! MODULE PR_BAYES_CHAR  : Grouped following COMMONS: 
!                         XERRWD_COMMON
!
! MODULE PR_BAYES_LOG  : Grouped following COMMONS: 
!                        SADVAN2_COMMON2
!
! ARGUMENTS   : NONE
!
! CALLED BY   : Used by Most of the PREDPP Routines
!
! CALLS       : NONE
!
! ALGORITHM   : NONE
!               
! MODULES USED: PRSIZES
!
! CONTAINS    : NONE
!
! LOCAL'S     : NONE
!
!---------------------------- END OF HEADER -----------------------------------------
!
      MODULE PRMOD_REAL
!
        USE PRSIZES, ONLY : ISIZE,DPSIZE,PC,PE
!
        REAL(KIND=DPSIZE) :: TWO,FOUR,A_0(PC),DA_0(PC,PE),D2A_0(PC,PE,PE)
!
      END MODULE PRMOD_REAL      
!
!+++
!
      MODULE PRMOD_CHAR      
!
        USE PRSIZES, ONLY : PC
!       
        CHARACTER(LEN=8) :: NAME(PC)
!        
      END MODULE PRMOD_CHAR      
!
!+++
!
      MODULE PRCM_INT
!
        USE PRSIZES, ONLY : ISIZE,PC,PIR,PE ! PM = PC-1
!
        INTEGER(KIND=ISIZE), PARAMETER :: PM=PC-1
!
        INTEGER(KIND=ISIZE) :: LST,LEND,SLEND(PE),SLST(PE),SPW,SLOOP,XLNCM1,XLNCM2,LNCM1,LNCM2,MFLAG1,      &
                               MFLAG2,MFLAG3,MFLAG4,MFLAG5,MFLAG6,MFLAG7,MFLAG8,SS3,XNC,XNCM1,MAP(PC),      &
                               MAPINV(PC),NBRON,XNBRON,PRMC(6),PRME(6),PRMG(6),PRMT(6),ITOTL(PC),INRD(PM),  &
                               NIAI,NIPI,NIT,XNIAI,XNIPI,XNIT,IAI(PIR),IAJ(PIR),IAK(PIR),IPI(PIR),IPJ(PIR), &
                               IPK(PIR),ITI(PIR),ITK(PIR),XIAI(PIR),XIAJ(PIR),XIAK(PIR),XIPI(PIR),XIPJ(PIR),&
                               XIPK(PIR),XITI(PIR),XITK(PIR),IAC(PIR),IPC(PIR),ITC(PIR),NIAI1,NIPI1,NIT1,   &
                               XNIAI1,XNIPI1,XNIT1,AA(PIR),PP(PIR),TT(PIR),XAA(PIR),XPP(PIR),XTT(PIR)
!
      END MODULE PRCM_INT      
!
!+++
!
      MODULE PRCM_REAL
!
        USE PRSIZES, ONLY : ISIZE,DPSIZE,PE,PC,PIR ! PE=LVR  PM=PC-1,P8=PM*2    P8=(PC-1)*2
!
        INTEGER(KIND=ISIZE), PARAMETER :: PM=PC-1,P8=(PC-1)*2
!
        REAL(KIND=DPSIZE) :: CINV11(3,3),CI11(3,6,PE+1),C11(3,3,PE+1),WS11(3,PE+1),ZS11(3,3,3,PE+1), &
                             CINV12(4,4),CI12(4,8,PE+1),C12(4,4,PE+1),WS12(4,PE+1),ZS12(4,4,4,PE+1), & 
                             SSAE2(PE,PE),SSRE2(PE,PE),SDELE2(PE,PE),RHOE2(PE,PE),CGM(PM,PM,PE+1),   &
                             CINVGM(PM,PM),CIGM(PM,P8,PE+1),BGM(P8,P8,PE+1),PWORKGM(P8,P8,PE+1),     &
                             DAC(PIR,1),DPC(PIR,1),DTC(PIR)
!
        REAL(KIND=DPSIZE) :: CTOP(3,3),CTOP1(4,4),CTOP5(PM,PM)
!        
        EQUIVALENCE (C11,CTOP)  ! LOCAL IN ADVAN11 AND SS11; Needed just to pass inputs to IMSL routine
        EQUIVALENCE (C12,CTOP1) ! LOCAL IN ADVAN12 AND SS12; Needed just to pass inputs to IMSL routine
        EQUIVALENCE (CGM,CTOP5) ! LOCAL IN ADVAN5,ADVAN7,SS5,SS7; Needed just to pass inputs to IMSL routine  
!
      END MODULE PRCM_REAL      
!
!+++
!
      MODULE PRCOM_INT
!
        USE PRSIZES, ONLY : ISIZE,PG,PC,PCT
!
        INTEGER(KIND=ISIZE),PARAMETER :: PM=PC-1
!
        INTEGER(KIND=ISIZE) :: NP,NBP,YFORM,MAXKF,IFORM(PG+1),IDC,IDO,MAXIC,ISV(PC),LOGUNT,& ! IDC -> IDD; IDD only used in ADVAN10
                               IINST(PC),ITURN(PC),JCONT,JTIME,JEVENT,JAMT,JRATE,JSS,ILIB, &
                               JDELTA,JCOMPT,JCOMPF,JERROR,SSC,KREC,JMORE,JDUM,IBF(PC),NC, &
                               IRR(PC),IS(PC),ID(PC),ITSC,IFR,ILAG(PC),ITRANS,IRGG,IREV,   &
                               NPETAS,NPEPS,ISPEC,DD(90),DCTR,BETA(90),IPOOL(90),IP,IHEAD, &
                               INEXT(90),IBACK(90),SV(PC),ADVID,SSID,IERRA,IPKA0,IPKA,ISUB,&  ! BETADM -> BETA
                               CALLER,CALLPK,CALLID,CALLA9,CPYTHE,NRD,MCOMP,NCM1,IH,MAXCAL,&
                               CALCTR,MITER,METH,IMAX,ISTFLG,INTFLG,IXUSER,XNPETA,MTNO,    &
                               IMTBEG,IMTEND,IMTGG(PCT),IMT(PCT),MTCNTR,MTPTR,IATT(PM,9),  &
                               LINK(PM,PC),ILINK(PM,PC),INUM(PM),IDNO,MMAX,MCNTR,KDES,KTOL,&
                               KAES,NOPROB,PKPROB,ERPROB,NFPR
!     
!        INTEGER(KIND=ISIZE) :: IDEF(7,PC)  ! IDEF diffined differently in PRED and PREDI
!        INTEGER IDEF2(7,PC)                ! local in PREDI
!        EQUIVALENCE (IDEF(1),IDEF2(1,1))   ! Used as common right now   
!
      END MODULE PRCOM_INT      
!      
!+++
!
      MODULE PRCOM_REAL
!
        USE PRSIZES, ONLY : ISIZE,DPSIZE,PE,PG,PC !PE =LVR PM = PC-1
!
        INTEGER(KIND=ISIZE), PARAMETER :: PM=PC-1
!
        REAL(KIND=DPSIZE) :: G3(PG+1,PE+1,PE+1),HH(PE,PE+1),DT,DTE(PE),DELTA,YMULT,     & !7.2E ! G3 -> GG
                             ZERO,ONE,XR,XD,TSTART,DTSTAR(PE),DDELTA(PE),D2DELT(PE,PE), &
                             ADTE(PE),D2ADTE(PE,PE),IA(90),IAA(90),IAEA(90,PE),IRA(90), &
                             IDA(90),IREA(90,PE),IDEA(90,PE),R(PC),RE(PC,PE),SDELE(PE), &
                             RHOE(PE),SSAE(PE),SSRE(PE),RHO,SDEL,SSA,SSR,SDEL1,         &
                             I2REA(90,PE,PE),SAMT,I2DEA(90,PE,PE),I2AEA(90,PE,PE),      &
                             R2E(PC,PE,PE),D2DTE(PE,PE),D2TSTA(PE,PE),DA(PM,PM),        &
                             DP(PM,PG),HR(PM,2),DET(PM),HETA(PM,PE,2),H2ETA(PM,PE,PE,2),&
                             DTINT(PE),D2TINT(PE,PE)
!
!        REAL(KIND=DPSIZE) :: GG(PG+1,PE+1)  ! local in PREDI 5.5b4. Caused trouble in gfortran/g95.
!        
!        EQUIVALENCE (GG(1,1),G3(1,1,1)) ! 5.5b4.  caused trouble in gfortran/g95
!
      END MODULE PRCOM_REAL      
!
!+++
!
      MODULE PROCM_INT
! 
        USE PRSIZES, ONLY : ISIZE,PE,PCT
!
        INTEGER(KIND=ISIZE) :: PNEWIF,NACTIV,ISFINL,A_0FLG,IDXETA(0:PE), &  ! M -> IDXETA
                               MTNOW,MTPAST(0:PCT),MTNEXT(0:PCT),NEVENT     ! N -> NEVENT
!
      END MODULE PROCM_INT      
!      
!+++
!
      MODULE PROCM_REAL
!
        USE PRSIZES, ONLY : ISIZE,DPSIZE,PE,PD,PC
!
        REAL(KIND=DPSIZE) :: DDOST(PE),D2DOST(PE,PE),DOSTIM,DOSREC(PD),TSTATE, &
                             EVTREC(5,PD+1),AMNT(PC),DAETA(PC,PE),D2AETA(PC,PE,PE)     ! A -> AMNT
!
        REAL(KIND=DPSIZE), ALLOCATABLE :: THETAS(:)
!
      END MODULE PROCM_REAL      
!
!+++
!
! TL commons: PRINFN, PRDPK1
!      MODULE TLCOM
!        USE PRSIZES, ONLY : DPSIZE,DIMTMP
!
!        REAL(KIND=DPSIZE) :: ITV(DIMTMP)
!             
!      END MODULE TLCOM
! TL commons: Replaces PRDPK1
!
!+++
!
      MODULE PKERR_REAL
!      
        USE PRSIZES, ONLY : DPSIZE,PCT
!
        REAL(KIND=DPSIZE) :: MTIME(PCT)
!
      END MODULE PKERR_REAL
!
!+++ BAYES MODEL Modules
!
      MODULE PR_BAYES_REAL2
!
        USE PRSIZES, ONLY: ISIZE,DPSIZE,PE,PC
!
        INTEGER(KIND=ISIZE), PARAMETER :: MAXRECID=200
!
        REAL(KIND=DPSIZE) :: STSTATE(MAXRECID),SA(PC,MAXRECID),SDAETA(PC,PE,MAXRECID),SD2AETA(PC,PE,PE,MAXRECID)
!
      END MODULE PR_BAYES_REAL2