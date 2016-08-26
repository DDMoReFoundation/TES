!*********************************COPYRIGHT******************************************
!                                                                                   !
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
! AUTHOR      : SAM THOMSEN, MARK SALE, AND ROBERT BAUER 
! CREATED ON  : MAR/2010
! LANGUAGE    : FORTRAN 90/95
! LAST UPDATE : MAR/2010 - CREATED
!               FEB/2011 - INTEGRATED 7.2BETA5.8B MODIFICATIONS
!               
!----------------------------PNM_PRGLOBAL1_RW.F90 ---------------------------------
!
! SUBROUTINE PNM_PRGLOBAL1_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
! DESCRIPTION : Send/receive predpp data between manager/worker beginning of every estimation
!
! ARGUMENTS   : TEMP_DIR,IOPEN,ICLOSE,IRW
!               IN     - TEMP_DIR,IOPEN,ICLOSE,IRW
!                        TEMP_DIR - Path for temporary directory.
!                        IOPEN    - Flag for opening a file.
!                        ICLOSE   - Flag for closing a file.
!                        IRW      - Read/write flag.
!               OUT    - NONE
!               IN OUT - NONE
!
! CALLED BY   : NONE
!
! CALLS       : PNM_WRITE_BEGIN,PNM_READ_BEGIN,PNM_RW_I,PNM_RW_IV,PNM_RW_R,PNM_RW_RV,
!               PNM_RW_REAL_ARRAY,PNM_RW_L,PNM_WRITE_END,PNM_READ_END
!
! ALGORITHM   : 
!
! MODULES USED: PNM_CONFIG,PRMOD_INT,PRGEAR_INT,PRCM_INT,PRMOD_REAL,PRGEAR_REAL,
!               PRCM_REAL,PKERR_REAL,PRMOD_CHAR,PRGEAR_CHAR,PRCM_CHAR,PRCM_LOG,PRCM2_REAL
!
! CONTAINS    : NONE
!
! LOCAL'S     : NONE
!
!---------------------------- END OF HEADER -----------------------------------------
!
      SUBROUTINE PNM_PRGLOBAL1_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
      USE PNM_CONFIG
! INTEGER
      USE PRMOD_INT
      USE PRGEAR_INT
      USE PRCM_INT
! REAL
      USE PRMOD_REAL
      USE PRGEAR_REAL
      USE PRCM_REAL
      USE PRCM2_REAL
      USE PKERR_REAL
! CHARACTER      
      USE PRMOD_CHAR
      USE PRGEAR_CHAR
      USE PRCM_CHAR
! LOGICAL     
      USE PRCM_LOG
!     
      IMPLICIT NONE
!
! INTEGER (KIND=ISIZE) used then during compilation following error we get
! Error: The same named entity from different modules and/or program units cannot be referenced.   [ISIZE]
      INTEGER,            INTENT(IN) :: IOPEN,ICLOSE,IRW 
      CHARACTER(LEN=256), INTENT(IN) :: TEMP_DIR
!      
!------------------------------------------------------------------------------------      
!
! Local Variables
!
      IF (IOPEN == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal1.dat')
        ELSE
          CALL PNM_READ_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal1.dat')
        END IF 
      END IF
!
! PRMOD_INT
      CALL PNM_RW_I(IKE)
      CALL PNM_RW_I(IK12)
      CALL PNM_RW_I(IK21)
      CALL PNM_RW_I(IV)
      CALL PNM_RW_I(KP)
      CALL PNM_RW_I(KC)
      CALL PNM_RW_I(IKA)
      CALL PNM_RW_I(KA)
      CALL PNM_RW_I(ICALLD)
      CALL PNM_RW_IV(IDEFD,M_IDEFD)
      CALL PNM_RW_IV(IDEFA,M_IDEFA)
      CALL PNM_RW_I(IOUT)
      CALL PNM_RW_I(IPRINT)
      CALL PNM_RW_I(MTDIFF)
      CALL PNM_RW_I(I_SS)
      CALL PNM_RW_I(ISSNOW)
      CALL PNM_RW_I(ISSMOD)
!
! PRMOD_REAL
      CALL PNM_RW_R(TWO)
      CALL PNM_RW_R(FOUR)
!      
! Arrays
! PRGEAR_INT
      CALL PNM_RW_I(NCGEAR)
      CALL PNM_RW_I(MFCGEAR)
      CALL PNM_RW_I(KFLAG)
      CALL PNM_RW_I(JSTART)
      CALL PNM_RW_I(NSQ)
      CALL PNM_RW_I(NQUSED)
      CALL PNM_RW_I(NSTEP)
      CALL PNM_RW_I(NFE)
      CALL PNM_RW_I(NJE)
      CALL PNM_RW_I(NPW)
      CALL PNM_RW_I(NERROR)
      CALL PNM_RW_I(NSAVE1)
      CALL PNM_RW_I(NSAVE2)
      CALL PNM_RW_I(NEQUIL)
      CALL PNM_RW_I(NY)
      CALL PNM_RW_I(I)
      CALL PNM_RW_I(METH)
      CALL PNM_RW_I(MITER)
      CALL PNM_RW_I(NQ)
      CALL PNM_RW_I(L)
      CALL PNM_RW_I(IDOUB)
      CALL PNM_RW_I(MFOLD)
      CALL PNM_RW_I(NOLD)
      CALL PNM_RW_I(IRET)
      CALL PNM_RW_I(MEO)
      CALL PNM_RW_I(MIO)
      CALL PNM_RW_I(IWEVAL)
      CALL PNM_RW_I(MAXDER)
      CALL PNM_RW_I(LMAX)
      CALL PNM_RW_I(IREDO)
      CALL PNM_RW_I(J)
      CALL PNM_RW_I(NSTEPJ)
      CALL PNM_RW_I(J1)
      CALL PNM_RW_I(J2)
      CALL PNM_RW_I(M)
      CALL PNM_RW_I(NEWQ)
      CALL PNM_RW_I(N0)
      CALL PNM_RW_I(NHCUT)
      CALL PNM_RW_I(NLCDIAG)
      CALL PNM_RW_I(NUCDIAG)
!
! PRGEAR_REAL
      CALL PNM_RW_R(T)
      CALL PNM_RW_R(HGEAR)
      CALL PNM_RW_R(HMIN)
      CALL PNM_RW_R(HMAX)
      CALL PNM_RW_R(EPSC)
      CALL PNM_RW_R(UROUND)
      CALL PNM_RW_R(EPSJ)
      CALL PNM_RW_R(HUSED)
      CALL PNM_RW_RV(EL,M_EL(1))
      CALL PNM_RW_R(OLDL0)
      CALL PNM_RW_R(TOLD)
      CALL PNM_RW_R(RMAX)
      CALL PNM_RW_R(RC)
      CALL PNM_RW_R(CRATE)
      CALL PNM_RW_R(EPSOLD)
      CALL PNM_RW_R(HOLD)
      CALL PNM_RW_R(FN)
      CALL PNM_RW_R(EDN)
      CALL PNM_RW_R(E)
      CALL PNM_RW_R(EUP)
      CALL PNM_RW_R(BND)
      CALL PNM_RW_R(RH)
      CALL PNM_RW_R(R1)
      CALL PNM_RW_R(R)
      CALL PNM_RW_R(HL0)
      CALL PNM_RW_R(R0)
      CALL PNM_RW_R(D)
      CALL PNM_RW_R(PHL0)
      CALL PNM_RW_R(PR3)
      CALL PNM_RW_R(D1)
      CALL PNM_RW_R(ENQ3)
      CALL PNM_RW_R(ENQ2)
      CALL PNM_RW_R(PR2)
      CALL PNM_RW_R(PR1)
      CALL PNM_RW_R(ENQ1)
      CALL PNM_RW_R(TOUTP)
      CALL PNM_RW_RV(TQ,M_TQ)
!                     
! PRCM_INT
      CALL PNM_RW_I(LST)
      CALL PNM_RW_I(LEND)
      CALL PNM_RW_I(SPW)
      CALL PNM_RW_I(SLOOP)
      CALL PNM_RW_I(XLNCM1)
      CALL PNM_RW_I(XLNCM2)
      CALL PNM_RW_I(LNCM1)
      CALL PNM_RW_I(LNCM2)
      CALL PNM_RW_I(MFLAG1)
      CALL PNM_RW_I(MFLAG2)
      CALL PNM_RW_I(MFLAG3)
      CALL PNM_RW_I(MFLAG4)
      CALL PNM_RW_I(MFLAG5)
      CALL PNM_RW_I(MFLAG6)
      CALL PNM_RW_I(MFLAG7)
      CALL PNM_RW_I(MFLAG8)
      CALL PNM_RW_I(SS3)
      CALL PNM_RW_I(XNC)
      CALL PNM_RW_I(XNCM1)
      CALL PNM_RW_I(NBRON)
      CALL PNM_RW_I(XNBRON)
      CALL PNM_RW_I(NIAI)
      CALL PNM_RW_I(NIPI)
      CALL PNM_RW_I(NIT)
      CALL PNM_RW_I(XNIAI)
      CALL PNM_RW_I(XNIPI)
      CALL PNM_RW_I(XNIT)
      CALL PNM_RW_I(NIAI1)
      CALL PNM_RW_I(NIPI1)
      CALL PNM_RW_I(NIT1)
      CALL PNM_RW_I(XNIAI1)
      CALL PNM_RW_I(XNIPI1)
      CALL PNM_RW_I(XNIT1)
!      
! Arrays
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DAC,M_DAC(1),M_DAC(2),M_DAC(3),M_DAC(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DPC,M_DPC(1),M_DPC(2),M_DPC(3),M_DPC(4))
!
! PRCM_LOG
      CALL PNM_RW_L(CALLP)
      CALL PNM_RW_L(CALLE)
      CALL PNM_RW_L(CALL9)
      CALL PNM_RW_L(TIMDEF)
      CALL PNM_RW_L(DIDCAA)
      CALL PNM_RW_L(DIDDES)
      CALL PNM_RW_L(DIDAES)
      CALL PNM_RW_L(DOFINL)
      CALL PNM_RW_L(HAVINV)
      CALL PNM_RW_L(HAVCB)
      CALL PNM_RW_L(GENMOD)
      CALL PNM_RW_L(MAPPED)
      CALL PNM_RW_L(COMPAC)
!
! Close file
      IF (ICLOSE == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_END()
        ELSE
          CALL PNM_READ_END()
        END IF
      END IF
!      
  999 RETURN
!      
      END SUBROUTINE PNM_PRGLOBAL1_RW
!
!-----------------------------HISTORY------------------------------------------------
! VERSION     : NONMEM VII
! AUTHOR      : SAM THOMSEN, MARK SALE, AND ROBERT BAUER 
! CREATED ON  : MAR/2010
! LANGUAGE    : FORTRAN 90/95
! LAST UPDATE : MAR/2010 - CREATED
!               FEB/2011 - INTEGRATED 7.2BETA5.8B MODIFICATIONS
!               
!----------------------------PNM_PRGLOBAL1_INIT_RW.F90 ---------------------------------
!
! SUBROUTINE PNM_PRGLOBAL1_INIT_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
! DESCRIPTION : Send/receive predpp data between manager/worker beginning of an estimation
!
! ARGUMENTS   : TEMP_DIR,IOPEN,ICLOSE,IRW
!               IN     - TEMP_DIR,IOPEN,ICLOSE,IRW
!                        TEMP_DIR - Path for temporary directory.
!                        IOPEN    - Flag for opening a file.
!                        ICLOSE   - Flag for closing a file.
!                        IRW      - Read/write flag.
!               OUT    - NONE
!               IN OUT - NONE
!
! CALLED BY   : (PNM_PROCESS),PNM_TASK_EXECUTE
!
! CALLS       : PNM_WRITE_BEGIN,PNM_READ_BEGIN,PNM_RW_REAL_ARRAY,PNM_RW_INT_ARRAY,
!               PNM_RW_CV,PNM_WRITE_END,PNM_READ_END
!
! ALGORITHM   : 
!
! MODULES USED: PNM_CONFIG,PRMOD_INT,PRGEAR_INT,PRCM_INT,PRMOD_REAL,PRGEAR_REAL,PRCM_REAL,
!               PKERR_REAL,PRMOD_CHAR,PRGEAR_CHAR,PRCM_CHAR,PRCM_LOG,PRCM2_REAL,NMBAYES_INT
!
! CONTAINS    : NONE
!
! LOCAL'S     : NONE
!
!---------------------------- END OF HEADER -----------------------------------------
!
      SUBROUTINE PNM_PRGLOBAL1_INIT_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
      USE PNM_CONFIG
! INTEGER
      USE PRMOD_INT
      USE PRGEAR_INT
      USE PRCM_INT
      USE NMBAYES_INT, ONLY: ATOL !7.2b55a
! REAL
      USE PRMOD_REAL
      USE PRGEAR_REAL
      USE PRCM_REAL
      USE PRCM2_REAL
      USE PKERR_REAL
! CHARACTER
      USE PRMOD_CHAR      
      USE PRGEAR_CHAR      
      USE PRCM_CHAR
! LOGICAL
      USE PRCM_LOG      
!
      IMPLICIT NONE
!
! INTEGER (KIND=ISIZE) used then during compilation following error we get
! Error: The same named entity from different modules and/or program units cannot be referenced.   [ISIZE]
      INTEGER,            INTENT(IN) :: IOPEN,ICLOSE,IRW 
      CHARACTER(LEN=256), INTENT(IN) :: TEMP_DIR
!      
!------------------------------------------------------------------------------------      
!
! Local Variables
!
      IF (IOPEN == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal1_init.dat')
        ELSE
          CALL PNM_READ_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal1_init.dat')
        END IF 
      END IF
!
! Arrays
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,A_0,M_A_0(1),M_A_0(2),M_A_0(3),M_A_0(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DA_0,M_DA_0(1),M_DA_0(2),M_DA_0(3),M_DA_0(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,D2A_0,M_D2A_0(1),M_D2A_0(2),M_D2A_0(3),M_D2A_0(4))
!                          
! Arrays
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,SLEND,M_SLEND(1),M_SLEND(2),M_SLEND(3),M_SLEND(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,SLST,M_SLST(1),M_SLST(2),M_SLST(3),M_SLST(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,MAP,M_MAP(1),M_MAP(2),M_MAP(3),M_MAP(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,MAPINV,M_MAPINV(1),M_MAPINV(2),M_MAPINV(3),M_MAPINV(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,PRMC,M_PRMC(1),M_PRMC(2),M_PRMC(3),M_PRMC(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,PRME,M_PRME(1),M_PRME(2),M_PRME(3),M_PRME(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,PRMG,M_PRMG(1),M_PRMG(2),M_PRMG(3),M_PRMG(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,PRMT,M_PRMT(1),M_PRMT(2),M_PRMT(3),M_PRMT(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ITOTL,M_ITOTL(1),M_ITOTL(2),M_ITOTL(3),M_ITOTL(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,INRD,M_INRD(1),M_INRD(2),M_INRD(3),M_INRD(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IAI,M_IAI(1),M_IAI(2),M_IAI(3),M_IAI(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IAJ,M_IAJ(1),M_IAJ(2),M_IAJ(3),M_IAJ(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IAK,M_IAK(1),M_IAK(2),M_IAK(3),M_IAK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IPI,M_IPI(1),M_IPI(2),M_IPI(3),M_IPI(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IPJ,M_IPJ(1),M_IPJ(2),M_IPJ(3),M_IPJ(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IPK,M_IPK(1),M_IPK(2),M_IPK(3),M_IPK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ITI,M_ITI(1),M_ITI(2),M_ITI(3),M_ITI(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ITK,M_ITK(1),M_ITK(2),M_ITK(3),M_ITK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XIAI,M_XIAI(1),M_XIAI(2),M_XIAI(3),M_XIAI(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XIAJ,M_XIAJ(1),M_XIAJ(2),M_XIAJ(3),M_XIAJ(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XIAK,M_XIAK(1),M_XIAK(2),M_XIAK(3),M_XIAK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XIPI,M_XIPI(1),M_XIPI(2),M_XIPI(3),M_XIPI(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XIPJ,M_XIPJ(1),M_XIPJ(2),M_XIPJ(3),M_XIPJ(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XIPK,M_XIPK(1),M_XIPK(2),M_XIPK(3),M_XIPK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XITI,M_XITI(1),M_XITI(2),M_XITI(3),M_XITI(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XITK,M_XITK(1),M_XITK(2),M_XITK(3),M_XITK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IAC,M_IAC(1),M_IAC(2),M_IAC(3),M_IAC(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IPC,M_IPC(1),M_IPC(2),M_IPC(3),M_IPC(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ITC,M_ITC(1),M_ITC(2),M_ITC(3),M_ITC(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,AA,M_AA(1),M_AA(2),M_AA(3),M_AA(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,PP,M_PP(1),M_PP(2),M_PP(3),M_PP(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,TT,M_TT(1),M_TT(2),M_TT(3),M_TT(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XAA,M_XAA(1),M_XAA(2),M_XAA(3),M_XAA(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XPP,M_XPP(1),M_XPP(2),M_XPP(3),M_XPP(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,XTT,M_XTT(1),M_XTT(2),M_XTT(3),M_XTT(4))
!
! PRCM_REAL
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CINV11,M_CINV11(1),M_CINV11(2),M_CINV11(3),M_CINV11(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CI11,M_CI11(1),M_CI11(2),M_CI11(3),M_CI11(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,C11,M_C11(1),M_C11(2),M_C11(3),M_C11(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,WS11,M_WS11(1),M_WS11(2),M_WS11(3),M_WS11(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,ZS11,M_ZS11(1),M_ZS11(2),M_ZS11(3),M_ZS11(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CINV12,M_CINV12(1),M_CINV12(2),M_CINV12(3),M_CINV12(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CI12,M_CI12(1),M_CI12(2),M_CI12(3),M_CI12(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,C12,M_C12(1),M_C12(2),M_C12(3),M_C12(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,WS12,M_WS12(1),M_WS12(2),M_WS12(3),M_WS12(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,ZS12,M_ZS12(1),M_ZS12(2),M_ZS12(3),M_ZS12(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,SSAE2,M_SSAE2(1),M_SSAE2(2),M_SSAE2(3),M_SSAE2(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,SSRE2,M_SSRE2(1),M_SSRE2(2),M_SSRE2(3),M_SSRE2(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,SDELE2,M_SDELE2(1),M_SDELE2(2),M_SDELE2(3),M_SDELE2(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,RHOE2,M_RHOE2(1),M_RHOE2(2),M_RHOE2(3),M_RHOE2(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CGM,M_CGM(1),M_CGM(2),M_CGM(3),M_CGM(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CINVGM,M_CINVGM(1),M_CINVGM(2),M_CINVGM(3),M_CINVGM(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CIGM,M_CIGM(1),M_CIGM(2),M_CIGM(3),M_CIGM(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,BGM,M_BGM(1),M_BGM(2),M_BGM(3),M_BGM(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,PWORKGM,M_PWORKGM(1),M_PWORKGM(2),M_PWORKGM(3),M_PWORKGM(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DTC,M_DTC(1),M_DTC(2),M_DTC(3),M_DTC(4))
!
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CTOP,M_CTOP(1),M_CTOP(2),M_CTOP(3),M_CTOP(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CTOP1,M_CTOP1(1),M_CTOP1(2),M_CTOP1(3),M_CTOP1(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,CTOP5,M_CTOP5(1),M_CTOP5(2),M_CTOP5(3),M_CTOP5(4))
!
! PRCM2_REAL
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,WORST,M_WORST(1),M_WORST(2),M_WORST(3),M_WORST(4))
! PKERR_REAL
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,MTIME,M_MTIME(1),M_MTIME(2),M_MTIME(3),M_MTIME(4))
!
! PRMOD_CHAR
      CALL PNM_RW_I(ATOL)
      CALL PNM_RW_CV(NAME,MP_PC)
!
! Close file
      IF (ICLOSE == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_END()
        ELSE
          CALL PNM_READ_END()
        END IF
      END IF
!      
  999 RETURN
!      
      END SUBROUTINE PNM_PRGLOBAL1_INIT_RW
!
!-----------------------------HISTORY------------------------------------------------
! VERSION     : NONMEM VII
! AUTHOR      : SAM THOMSEN, MARK SALE, AND ROBERT BAUER 
! CREATED ON  : MAR/2010
! LANGUAGE    : FORTRAN 90/95
! LAST UPDATE : MAR/2010 - CREATED
!               
!----------------------------PNM_PRGLOBAL2_RW.F90 ---------------------------------
!
! SUBROUTINE PNM_PRGLOBAL2_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
! DESCRIPTION : Send/receive predpp data between manager/worker beginning of every estimation
!
! ARGUMENTS   : TEMP_DIR,IOPEN,ICLOSE,IRW
!               IN     - TEMP_DIR,IOPEN,ICLOSE,IRW
!                        TEMP_DIR - Path for temporary directory.
!                        IOPEN    - Flag for opening a file.
!                        ICLOSE   - Flag for closing a file.
!                        IRW      - Read/write flag.
!               OUT    - NONE
!               IN OUT - NONE
!
! CALLED BY   : (PNM_PROCESS),PNM_TASK_EXECUTE
!
! CALLS       : PNM_WRITE_BEGIN,PNM_READ_BEGIN,PNM_RW_I,PNM_RW_R,PNM_RW_REAL_ARRAY,
!               PNM_WRITE_END,PNM_READ_END
!
! ALGORITHM   : 
!
! MODULES USED: PNM_CONFIG,PRLS01_INT,PRLS01_REAL,PRLS01_CHAR
!
! CONTAINS    : NONE
!
! LOCAL'S     : NONE
!
!---------------------------- END OF HEADER -----------------------------------------
!
      SUBROUTINE PNM_PRGLOBAL2_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
      USE PNM_CONFIG
! INTEGER      
      USE PRLS01_INT
! REAL
      USE PRLS01_REAL
! CHARACTER
      USE PRLS01_CHAR
!
      IMPLICIT NONE
!
! INTEGER (KIND=ISIZE) used then during compilation following error we get
! Error: The same named entity from different modules and/or program units cannot be referenced.   [ISIZE]
      INTEGER,             INTENT(IN) :: IOPEN,ICLOSE,IRW
      CHARACTER(LEN=256),  INTENT(IN) :: TEMP_DIR
!      
!------------------------------------------------------------------------------------      
!
! Local Variables
!
      IF (IOPEN == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal2.dat')
        ELSE
          CALL PNM_READ_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal2.dat')
        END IF 
      END IF
!
! PRLS01_INT
      CALL PNM_RW_I(ICF)
      CALL PNM_RW_I(IERPJ)
      CALL PNM_RW_I(IERSL)
      CALL PNM_RW_I(JCUR)
      CALL PNM_RW_I(JSTART)
      CALL PNM_RW_I(KFLAG)
      CALL PNM_RW_I(L)
      CALL PNM_RW_I(METH)
      CALL PNM_RW_I(MITER)
      CALL PNM_RW_I(MAXORD)
      CALL PNM_RW_I(MAXCOR)
      CALL PNM_RW_I(MSBP)
      CALL PNM_RW_I(MXNCF)
      CALL PNM_RW_I(N)
      CALL PNM_RW_I(NQ)
      CALL PNM_RW_I(NST)
      CALL PNM_RW_I(NRELSDI)
      CALL PNM_RW_I(NJE)
      CALL PNM_RW_I(NQU)
      CALL PNM_RW_I(ILLIN)
      CALL PNM_RW_I(INIT)
      CALL PNM_RW_I(LYH)
      CALL PNM_RW_I(LEWT)
      CALL PNM_RW_I(LACOR)
      CALL PNM_RW_I(LSAVR)
      CALL PNM_RW_I(LWM)
      CALL PNM_RW_I(LIWM)
      CALL PNM_RW_I(MXSTEP)
      CALL PNM_RW_I(MXHNIL)
      CALL PNM_RW_I(NHNIL)
      CALL PNM_RW_I(NTREP)
      CALL PNM_RW_I(NSLAST)
      CALL PNM_RW_I(NYH)
      CALL PNM_RW_I(IALTH)
      CALL PNM_RW_I(IPUP)
      CALL PNM_RW_I(LMAX)
      CALL PNM_RW_I(MEO)
      CALL PNM_RW_I(NQNYH)
      CALL PNM_RW_I(NSLP)
! 
      CALL PNM_RW_R(CCMAX)
      CALL PNM_RW_R(EL0)
      CALL PNM_RW_R(H)
      CALL PNM_RW_R(HMIN)
      CALL PNM_RW_R(HMXI)
      CALL PNM_RW_R(HU)
      CALL PNM_RW_R(RC)
      CALL PNM_RW_R(TN)
      CALL PNM_RW_R(UROUND)
      CALL PNM_RW_R(CONIT)
      CALL PNM_RW_R(CRATE)
      CALL PNM_RW_R(HOLD)
      CALL PNM_RW_R(RMAX)
!      
! Arrays
!      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,EL,M_EL(1),M_EL(2),M_EL(3),M_EL(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,ELCO,M_ELCO(1),M_ELCO(2),M_ELCO(3),M_ELCO(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,TESCO,M_TESCO(1),M_TESCO(2),M_TESCO(3),M_TESCO(4))
!
! Close file
      IF (ICLOSE == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_END()
        ELSE
          CALL PNM_READ_END()
        END IF
      END IF
!      
  999 RETURN
!
      END SUBROUTINE PNM_PRGLOBAL2_RW
!      
!-----------------------------HISTORY------------------------------------------------
! VERSION     : NONMEM VII
! AUTHOR      : SAM THOMSEN, MARK SALE, AND ROBERT BAUER 
! CREATED ON  : MAR/2010
! LANGUAGE    : FORTRAN 90/95
! LAST UPDATE : MAR/2010 - CREATED
!               
!----------------------------PNM_PRGLOBAL3_RW.F90 ---------------------------------
!
! SUBROUTINE PNM_PRGLOBAL3_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
! DESCRIPTION : Send/receive predpp data between manager/worker beginning of every estimation
!
! ARGUMENTS   : TEMP_DIR,IOPEN,ICLOSE,IRW
!               IN     - TEMP_DIR,IOPEN,ICLOSE,IRW
!                        TEMP_DIR - Path for temporary directory.
!                        IOPEN    - Flag for opening a file.
!                        ICLOSE   - Flag for closing a file.
!                        IRW      - Read/write flag.
!               OUT    - NONE
!               IN OUT - NONE
!
! CALLED BY   : NONE
!
! CALLS       : PNM_WRITE_BEGIN,PNM_READ_BEGIN,PNM_RW_I,PNM_RW_INT_ARRAY,PNM_RW_R,
!               PNM_RW_REAL_ARRAY,PNM_RW_IV,PNM_RW_L,PNM_WRITE_END,PNM_READ_END
!
! ALGORITHM   : 
!
! MODULES USED: PNM_CONFIG,PRCOM_INT,PROCM_INT,PRCOM_REAL,PROCM_REAL,PRCOM_CHAR,
!               PROCM_CHAR,PRCOM_LOG,PRCM2_REAL
!
! CONTAINS    : NONE
!
! LOCAL'S     : NONE
!
!---------------------------- END OF HEADER -----------------------------------------
!
!  SUBROUTINE PNM_PRGLOBAL2_READ
!
      SUBROUTINE PNM_PRGLOBAL3_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
      USE PNM_CONFIG
! INTEGER                            
      USE PRCOM_INT
      USE PROCM_INT      
! REAL      
      USE PRCOM_REAL
      USE PRCM2_REAL
      USE PROCM_REAL
! CHARACTER
      USE PRCOM_CHAR
      USE PROCM_CHAR
! LOGICAL      
      USE PRCOM_LOG      
!
      IMPLICIT NONE
!
! INTEGER (KIND=ISIZE) used then during compilation following error we get
! Error: The same named entity from different modules and/or program units cannot be referenced.   [ISIZE]
      INTEGER,            INTENT(IN) :: IOPEN,ICLOSE,IRW
      CHARACTER(LEN=256), INTENT(IN) :: TEMP_DIR
!
!------------------------------------------------------------------------------------      
!
! Local Variables
!
      IF (IOPEN == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal3.dat')
        ELSE
          CALL PNM_READ_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal3.dat')
        END IF 
      END IF
!
! PRCOM_INT
      CALL PNM_RW_I(NP)
      CALL PNM_RW_I(NBP)
      CALL PNM_RW_I(YFORM)
      CALL PNM_RW_I(MAXKF)
      CALL PNM_RW_I(IDC)
      CALL PNM_RW_I(IDO)
      CALL PNM_RW_I(MAXIC)
      CALL PNM_RW_I(LOGUNT)
      CALL PNM_RW_I(JCONT)
      CALL PNM_RW_I(JTIME)
      CALL PNM_RW_I(JEVENT)
      CALL PNM_RW_I(JAMT)
      CALL PNM_RW_I(JRATE)
      CALL PNM_RW_I(JSS)
      CALL PNM_RW_I(ILIB)
      CALL PNM_RW_I(JDELTA)
      CALL PNM_RW_I(JCOMPT)
      CALL PNM_RW_I(JCOMPF)
      CALL PNM_RW_I(JERROR)
      CALL PNM_RW_I(SSC)
      CALL PNM_RW_I(KREC)
      CALL PNM_RW_I(JMORE)
      CALL PNM_RW_I(JDUM)
      CALL PNM_RW_I(NC)
      CALL PNM_RW_I(ITSC)
      CALL PNM_RW_I(IFR)
      CALL PNM_RW_I(ITRANS)
      CALL PNM_RW_I(IRGG)
      CALL PNM_RW_I(IREV)
      CALL PNM_RW_I(NPETAS)
      CALL PNM_RW_I(NPEPS)
      CALL PNM_RW_I(ISPEC)
      CALL PNM_RW_I(DCTR)
      CALL PNM_RW_I(IP)
      CALL PNM_RW_I(IHEAD)
      CALL PNM_RW_I(ADVID)
      CALL PNM_RW_I(SSID)
      CALL PNM_RW_I(IERRA)
      CALL PNM_RW_I(IPKA0)
      CALL PNM_RW_I(IPKA)
      CALL PNM_RW_I(ISUB)
      CALL PNM_RW_I(CALLER)
      CALL PNM_RW_I(CALLPK)
      CALL PNM_RW_I(CALLID)
      CALL PNM_RW_I(CALLA9)
      CALL PNM_RW_I(CPYTHE)
      CALL PNM_RW_I(NRD)
      CALL PNM_RW_I(MCOMP)
      CALL PNM_RW_I(NCM1)
      CALL PNM_RW_I(IH)
      CALL PNM_RW_I(MAXCAL)
      CALL PNM_RW_I(CALCTR)
      CALL PNM_RW_I(MITER)
      CALL PNM_RW_I(METH)
      CALL PNM_RW_I(IMAX)
      CALL PNM_RW_I(ISTFLG)
      CALL PNM_RW_I(INTFLG)
      CALL PNM_RW_I(IXUSER)
      CALL PNM_RW_I(XNPETA)
      CALL PNM_RW_I(MTNO)
      CALL PNM_RW_I(IMTBEG)
      CALL PNM_RW_I(IMTEND)
      CALL PNM_RW_I(MTCNTR)
      CALL PNM_RW_I(MTPTR)
      CALL PNM_RW_I(IDNO)
      CALL PNM_RW_I(MMAX)
      CALL PNM_RW_I(MCNTR)
      CALL PNM_RW_I(KDES)
      CALL PNM_RW_I(KTOL)
      CALL PNM_RW_I(KAES)
      CALL PNM_RW_I(NOPROB)
      CALL PNM_RW_I(PKPROB)
      CALL PNM_RW_I(ERPROB)
      CALL PNM_RW_I(NFPR)
!
! Arrays
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,SV,M_SV(1),M_SV(2),M_SV(3),M_SV(4))
!
! PRCOM_REAL
      CALL PNM_RW_R(DT)
      CALL PNM_RW_R(DELTA)
      CALL PNM_RW_R(YMULT)
      CALL PNM_RW_R(ZERO)
      CALL PNM_RW_R(ONE)
      CALL PNM_RW_R(XR)
      CALL PNM_RW_R(XD)
      CALL PNM_RW_R(TSTART)
      CALL PNM_RW_R(RHO)
      CALL PNM_RW_R(SDEL)
      CALL PNM_RW_R(SSA)
      CALL PNM_RW_R(SSR)
      CALL PNM_RW_R(SDEL1)
      CALL PNM_RW_R(SAMT)
! PRCM2_REAL
      CALL PNM_RW_R(FAC)
!      
! Arrays
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,G3,M_G3(1),M_G3(2),M_G3(3),M_G3(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,HH,M_HH(1),M_HH(2),M_HH(3),M_HH(4))
!
! PRCOM_LOG
      CALL PNM_RW_L(NOETAS)
      CALL PNM_RW_L(SECOND)
      CALL PNM_RW_L(XNOETA)
      CALL PNM_RW_L(PF)
      CALL PNM_RW_L(DTIME)
      CALL PNM_RW_L(LFLAG)
      CALL PNM_RW_L(DTDER)
      CALL PNM_RW_L(NEWWAY)
!
! PROCM_INT
      CALL PNM_RW_I(PNEWIF)
      CALL PNM_RW_I(NACTIV)
      CALL PNM_RW_I(ISFINL)
      CALL PNM_RW_I(A_0FLG)
      CALL PNM_RW_IV(IDXETA(0),MP_PE+1)
      CALL PNM_RW_I(MTNOW)
      CALL PNM_RW_IV(MTPAST(0),MP_PCT+1)
      CALL PNM_RW_IV(MTNEXT(0),MP_PCT+1)
      CALL PNM_RW_I(NEVENT)
!
! PROCM_REAL
      CALL PNM_RW_R(DOSTIM)
      CALL PNM_RW_R(TSTATE)
!
! Arrays
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,EVTREC,M_EVTREC(1),M_EVTREC(2),M_EVTREC(3),M_EVTREC(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,AMNT,M_AMNT(1),M_AMNT(2),M_AMNT(3),M_AMNT(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DAETA,M_DAETA(1),M_DAETA(2),M_DAETA(3),M_DAETA(4))
!
! Close file
      IF (ICLOSE == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_END()
        ELSE
          CALL PNM_READ_END()
        END IF
      END IF
!      
  999 RETURN
!      
      END SUBROUTINE PNM_PRGLOBAL3_RW
!
!-----------------------------HISTORY------------------------------------------------
! VERSION     : NONMEM VII
! AUTHOR      : SAM THOMSEN, MARK SALE, AND ROBERT BAUER 
! CREATED ON  : MAR/2010
! LANGUAGE    : FORTRAN 90/95
! LAST UPDATE : MAR/2010 - CREATED
!               
!----------------------------PNM_PRGLOBAL3_INIT_RW.F90 ---------------------------------
!
! SUBROUTINE PNM_PRGLOBAL3_INIT_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)
!
! DESCRIPTION : Send/receive predpp data between manager/worker beginning of every estimation
!
! ARGUMENTS   : TEMP_DIR,IOPEN,ICLOSE,IRW
!               IN     - TEMP_DIR,IOPEN,ICLOSE,IRW
!                        TEMP_DIR - Path for temporary directory.
!                        IOPEN    - Flag for opening a file.
!                        ICLOSE   - Flag for closing a file.
!                        IRW      - Read/write flag.
!               OUT    - NONE
!               IN OUT - NONE
!
! CALLED BY   : (PNM_PROCESS),PNM_TASK_EXECUTE
!
! CALLS       : PNM_WRITE_BEGIN,PNM_READ_BEGIN,PNM_RW_INT_ARRAY,PNM_RW_REAL_ARRAY,
!               PNM_WRITE_END,PNM_READ_END
!
! ALGORITHM   : 
!
! MODULES USED: PNM_CONFIG,PRCOM_INT,PROCM_INT,PRCOM_REAL,PROCM_REAL,PRCOM_CHAR,
!               PROCM_CHAR,PRCOM_LOG
!
! CONTAINS    : NONE
!
! LOCAL'S     : NONE
!
!---------------------------- END OF HEADER -----------------------------------------
!
      SUBROUTINE PNM_PRGLOBAL3_INIT_RW(TEMP_DIR,IOPEN,ICLOSE,IRW)

      USE PNM_CONFIG
! INTEGER                            
      USE PRCOM_INT
      USE PROCM_INT
! REAL      
      USE PRCOM_REAL
      USE PROCM_REAL
! CHARACTER
      USE PRCOM_CHAR
      USE PROCM_CHAR
! LOGICAL      
      USE PRCOM_LOG          
!
      IMPLICIT NONE
!
! INTEGER (KIND=ISIZE) used then during compilation following error we get
! Error: The same named entity from different modules and/or program units cannot be referenced.   [ISIZE]
      INTEGER,            INTENT(IN) :: IOPEN,ICLOSE,IRW
      CHARACTER(LEN=256), INTENT(IN) :: TEMP_DIR
!
!------------------------------------------------------------------------------------      
!
! Local Variables
!
      IF (IOPEN == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal3_init.dat')
        ELSE
          CALL PNM_READ_BEGIN(PNM_UPAR,TRIM(TEMP_DIR) // 'prglobal3_init.dat')
        END IF 
      END IF
!
! PRCOM_INT
! Arrays
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IFORM,M_IFORM(1),M_IFORM(2),M_IFORM(3),M_IFORM(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ISV,M_ISV(1),M_ISV(2),M_ISV(3),M_ISV(4))      
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IINST,M_IINST(1),M_IINST(2),M_IINST(3),M_IINST(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ITURN,M_ITURN(1),M_ITURN(2),M_ITURN(3),M_ITURN(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IRR,M_IRR(1),M_IRR(2),M_IRR(3),M_IRR(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IS,M_IS(1),M_IS(2),M_IS(3),M_IS(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ID,M_ID(1),M_ID(2),M_ID(3),M_ID(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ILAG,M_ILAG(1),M_ILAG(2),M_ILAG(3),M_ILAG(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,DD,M_DD(1),M_DD(2),M_DD(3),M_DD(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,BETA,M_BETA(1),M_BETA(2),M_BETA(3),M_BETA(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IPOOL,M_IPOOL(1),M_IPOOL(2),M_IPOOL(3),M_IPOOL(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,INEXT,M_INEXT(1),M_INEXT(2),M_INEXT(3),M_INEXT(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IBACK,M_IBACK(1),M_IBACK(2),M_IBACK(3),M_IBACK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,SV,M_SV(1),M_SV(2),M_SV(3),M_SV(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IMTGG,M_IMTGG(1),M_IMTGG(2),M_IMTGG(3),M_IMTGG(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IMT,M_IMT(1),M_IMT(2),M_IMT(3),M_IMT(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,IATT,M_IATT(1),M_IATT(2),M_IATT(3),M_IATT(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,LINK,M_LINK(1),M_LINK(2),M_LINK(3),M_LINK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,ILINK,M_ILINK(1),M_ILINK(2),M_ILINK(3),M_ILINK(4))
      CALL PNM_RW_INT_ARRAY(PNM_UPAR,INUM,M_INUM(1),M_INUM(2),M_INUM(3),M_INUM(4))
!     
! Arrays
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DTE,M_DTE(1),M_DTE(2),M_DTE(3),M_DTE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DTSTAR,M_DTSTAR(1),M_DTSTAR(2),M_DTSTAR(3),M_DTSTAR(4))    
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DDELTA,M_DDELTA(1),M_DDELTA(2),M_DDELTA(3),M_DDELTA(4))  ! SAM 
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,D2DELT,M_D2DELT(1),M_D2DELT(2),M_D2DELT(3),M_D2DELT(4)) ! SAM
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,ADTE,M_ADTE(1),M_ADTE(2),M_ADTE(3),M_ADTE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,D2ADTE,M_D2ADTE(1),M_D2ADTE(2),M_D2ADTE(3),M_D2ADTE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,IA,M_IA(1),M_IA(2),M_IA(3),M_IA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,IAA,M_IAA(1),M_IAA(2),M_IAA(3),M_IAA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,IAEA,M_IAEA(1),M_IAEA(2),M_IAEA(3),M_IAEA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,IRA,M_IRA(1),M_IRA(2),M_IRA(3),M_IRA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,IDA,M_IDA(1),M_IDA(2),M_IDA(3),M_IDA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,IREA,M_IREA(1),M_IREA(2),M_IREA(3),M_IREA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,IDEA,M_IDEA(1),M_IDEA(2),M_IDEA(3),M_IDEA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,R,M_R(1),M_R(2),M_R(3),M_R(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,RE,M_RE(1),M_RE(2),M_RE(3),M_RE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,SDELE,M_SDELE(1),M_SDELE(2),M_SDELE(3),M_SDELE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,RHOE,M_RHOE(1),M_RHOE(2),M_RHOE(3),M_RHOE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,SSAE,M_SSAE(1),M_SSAE(2),M_SSAE(3),M_SSAE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,SSRE,M_SSRE(1),M_SSRE(2),M_SSRE(3),M_SSRE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,I2REA,M_I2REA(1),M_I2REA(2),M_I2REA(3),M_I2REA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,I2DEA,M_I2DEA(1),M_I2DEA(2),M_I2DEA(3),M_I2DEA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,I2AEA,M_I2AEA(1),M_I2AEA(2),M_I2AEA(3),M_I2AEA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,R2E,M_R2E(1),M_R2E(2),M_R2E(3),M_R2E(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,D2DTE,M_D2DTE(1),M_D2DTE(2),M_D2DTE(3),M_D2DTE(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,D2TSTA,M_D2TSTA(1),M_D2TSTA(2),M_D2TSTA(3),M_D2TSTA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DA,M_DA(1),M_DA(2),M_DA(3),M_DA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DP,M_DP(1),M_DP(2),M_DP(3),M_DP(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,HR,M_HR(1),M_HR(2),M_HR(3),M_HR(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DET,M_DET(1),M_DET(2),M_DET(3),M_DET(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,HETA,M_HETA(1),M_HETA(2),M_HETA(3),M_HETA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,H2ETA,M_H2ETA(1),M_H2ETA(2),M_H2ETA(3),M_H2ETA(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DTINT,M_DTINT(1),M_DTINT(2),M_DTINT(3),M_DTINT(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,D2TINT,M_D2TINT(1),M_D2TINT(2),M_D2TINT(3),M_D2TINT(4))
!
! Arrays
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DDOST,M_DDOST(1),M_DDOST(2),M_DDOST(3),M_DDOST(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,D2DOST,M_D2DOST(1),M_D2DOST(2),M_D2DOST(3),M_D2DOST(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,DOSREC,M_DOSREC(1),M_DOSREC(2),M_DOSREC(3),M_DOSREC(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,THETAS,M_THETAS(1),M_THETAS(2),M_THETAS(3),M_THETAS(4))
      CALL PNM_RW_REAL_ARRAY(PNM_UPAR,D2AETA,M_D2AETA(1),M_D2AETA(2),M_D2AETA(3),M_D2AETA(4))            
!
! Close file
      IF (ICLOSE == 1) THEN
        IF (IRW == 1) THEN
          CALL PNM_WRITE_END()
        ELSE
          CALL PNM_READ_END()
        END IF
      END IF
!      
  999 RETURN
!      
      END SUBROUTINE PNM_PRGLOBAL3_INIT_RW
!
!-----------------------------HISTORY------------------------------------------------
! VERSION     : NONMEM VII
! AUTHOR      : SAM THOMSEN, MARK SALE, AND ROBERT BAUER 
! CREATED ON  : MAR/2010
! LANGUAGE    : FORTRAN 90/95
! LAST UPDATE : MAR/2010 - CREATED
!               MAY/2010 - ADDED SIZE AND DO LOOPS
!               FEB/2011 - INTEGRATED 7.2BETA5.8B MODIFICATIONS
!               
!----------------------------PRDIMENSION_SETUP.F90 ---------------------------------
!
! SUBROUTINE PRDIMENSION_SETUP()
!
! DESCRIPTION : Get dimension information for predpp variables.
!
! ARGUMENTS   : NONE
!
! CALLED BY   : DIMENSION_SETUP
!
! CALLS       : NONE
!
! ALGORITHM   : 
!
! MODULES USED: PNM_CONFIG,PRMOD_INT,PRGEAR_INT,PRCM_INT,PRCOM_INT,PROCM_INT,PRMOD_REAL,
!               PRGEAR_REAL,PRCM_REAL,PKERR_REAL,PRCOM_REAL,PROCM_REAL,PRLS01_REAL,
!               PRMOD_CHAR,PRGEAR_CHAR,PRCM_CHAR,PRCOM_CHAR,PROCM_CHAR,PRCM_LOG,PRCOM_LOG,
!               PRCM2_REAL,FSIZES
!
! CONTAINS    : NONE
!
! LOCAL'S     : INIT_I,INIT_J
!
!---------------------------- END OF HEADER -----------------------------------------
!
      SUBROUTINE PRDIMENSION_SETUP()
!      
      USE FSIZES, ONLY: F_LTH
      USE PNM_CONFIG
! INTEGER                            
      USE PRMOD_INT
      USE PRGEAR_INT
      USE PRCM_INT
      USE PRCOM_INT
      USE PROCM_INT
! REAL      
      USE PRMOD_REAL
      USE PRGEAR_REAL, PRGEAR_REAL_R => R
      USE PRCM_REAL
      USE PRCM2_REAL
      USE PKERR_REAL
      USE PRCOM_REAL
      USE PROCM_REAL
      USE PRLS01_REAL, ONLY: TESCO,ELCO
! CHARACTER      
      USE PRMOD_CHAR
      USE PRGEAR_CHAR
      USE PRCM_CHAR
      USE PRCOM_CHAR
      USE PROCM_CHAR
! LOGICAL      
      USE PRCM_LOG
      USE PRCOM_LOG
!      
      IMPLICIT NONE
!
      SAVE
!      
!------------------------------------------------------------------------------------      
!
! Local Variables
!
! INTEGER (KIND=ISIZE) used then during compilation following error we get
! Error: The same named entity from different modules and/or program units cannot be referenced.   [ISIZE]
      INTEGER :: INIT_I,INIT_J,ALLOC_FLAG
!
      DATA ALLOC_FLAG /0/
!      
      MP_PE=SIZE(SLEND,1)
      MP_PC=SIZE(MAP,1)
      MP_PIR=SIZE(IAI,1)
      MP_P8=SIZE(BGM,1)
      MP_PCT=SIZE(MTIME,1)
      MP_PG=SIZE(IFORM,1)
      MP_PD=SIZE(DOSREC,1)
!      
      IF (ALLOC_FLAG == 0) THEN
        IF (.NOT. ALLOCATED(THETAS)) ALLOCATE(THETAS(F_LTH))
        ALLOC_FLAG=1
      END IF
!    
      MP_LTH=SIZE(THETAS,1)
! Init PR array dimensions
      INIT_I=SIZE(SHAPE(A_0)); M_A_0=1; DO INIT_J=1,INIT_I; M_A_0(INIT_J)=SIZE(A_0,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(AA)); M_AA=1; DO INIT_J=1,INIT_I; M_AA(INIT_J)=SIZE(AA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ADTE)); M_ADTE=1; DO INIT_J=1,INIT_I; M_ADTE(INIT_J)=SIZE(ADTE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(AMNT)); M_AMNT=1; DO INIT_J=1,INIT_I; M_AMNT(INIT_J)=SIZE(AMNT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(BETA)); M_BETA=1; DO INIT_J=1,INIT_I; M_BETA(INIT_J)=SIZE(BETA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(BGM)); M_BGM=1; DO INIT_J=1,INIT_I; M_BGM(INIT_J)=SIZE(BGM,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(C11)); M_C11=1; DO INIT_J=1,INIT_I; M_C11(INIT_J)=SIZE(C11,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(C12)); M_C12=1; DO INIT_J=1,INIT_I; M_C12(INIT_J)=SIZE(C12,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CGM)); M_CGM=1; DO INIT_J=1,INIT_I; M_CGM(INIT_J)=SIZE(CGM,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CI11)); M_CI11=1; DO INIT_J=1,INIT_I; M_CI11(INIT_J)=SIZE(CI11,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CI12)); M_CI12=1; DO INIT_J=1,INIT_I; M_CI12(INIT_J)=SIZE(CI12,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CIGM)); M_CIGM=1; DO INIT_J=1,INIT_I; M_CIGM(INIT_J)=SIZE(CIGM,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CINV11)); M_CINV11=1; DO INIT_J=1,INIT_I; M_CINV11(INIT_J)=SIZE(CINV11,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CINV12)); M_CINV12=1; DO INIT_J=1,INIT_I; M_CINV12(INIT_J)=SIZE(CINV12,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CINVGM)); M_CINVGM=1; DO INIT_J=1,INIT_I; M_CINVGM(INIT_J)=SIZE(CINVGM,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CTOP)); M_CTOP=1; DO INIT_J=1,INIT_I; M_CTOP(INIT_J)=SIZE(CTOP,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CTOP1)); M_CTOP1=1; DO INIT_J=1,INIT_I; M_CTOP1(INIT_J)=SIZE(CTOP1,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(CTOP5)); M_CTOP5=1; DO INIT_J=1,INIT_I; M_CTOP5(INIT_J)=SIZE(CTOP5,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(D2A_0)); M_D2A_0=1; DO INIT_J=1,INIT_I; M_D2A_0(INIT_J)=SIZE(D2A_0,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(D2ADTE)); M_D2ADTE=1; DO INIT_J=1,INIT_I; M_D2ADTE(INIT_J)=SIZE(D2ADTE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(D2AETA)); M_D2AETA=1; DO INIT_J=1,INIT_I; M_D2AETA(INIT_J)=SIZE(D2AETA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(D2DELT)); M_D2DELT=1; DO INIT_J=1,INIT_I; M_D2DELT(INIT_J)=SIZE(D2DELT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(D2DOST)); M_D2DOST=1; DO INIT_J=1,INIT_I; M_D2DOST(INIT_J)=SIZE(D2DOST,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(D2DTE)); M_D2DTE=1; DO INIT_J=1,INIT_I; M_D2DTE(INIT_J)=SIZE(D2DTE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(D2TINT)); M_D2TINT=1; DO INIT_J=1,INIT_I; M_D2TINT(INIT_J)=SIZE(D2TINT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(D2TSTA)); M_D2TSTA=1; DO INIT_J=1,INIT_I; M_D2TSTA(INIT_J)=SIZE(D2TSTA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DA)); M_DA=1; DO INIT_J=1,INIT_I; M_DA(INIT_J)=SIZE(DA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DA_0)); M_DA_0=1; DO INIT_J=1,INIT_I; M_DA_0(INIT_J)=SIZE(DA_0,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DAC)); M_DAC=1; DO INIT_J=1,INIT_I; M_DAC(INIT_J)=SIZE(DAC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DAETA)); M_DAETA=1; DO INIT_J=1,INIT_I; M_DAETA(INIT_J)=SIZE(DAETA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DD)); M_DD=1; DO INIT_J=1,INIT_I; M_DD(INIT_J)=SIZE(DD,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DDELTA)); M_DDELTA=1; DO INIT_J=1,INIT_I; M_DDELTA(INIT_J)=SIZE(DDELTA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DDOST)); M_DDOST=1; DO INIT_J=1,INIT_I; M_DDOST(INIT_J)=SIZE(DDOST,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DET)); M_DET=1; DO INIT_J=1,INIT_I; M_DET(INIT_J)=SIZE(DET,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DOSREC)); M_DOSREC=1; DO INIT_J=1,INIT_I; M_DOSREC(INIT_J)=SIZE(DOSREC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DP)); M_DP=1; DO INIT_J=1,INIT_I; M_DP(INIT_J)=SIZE(DP,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DPC)); M_DPC=1; DO INIT_J=1,INIT_I; M_DPC(INIT_J)=SIZE(DPC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DTC)); M_DTC=1; DO INIT_J=1,INIT_I; M_DTC(INIT_J)=SIZE(DTC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DTE)); M_DTE=1; DO INIT_J=1,INIT_I; M_DTE(INIT_J)=SIZE(DTE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DTINT)); M_DTINT=1; DO INIT_J=1,INIT_I; M_DTINT(INIT_J)=SIZE(DTINT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(DTSTAR)); M_DTSTAR=1; DO INIT_J=1,INIT_I; M_DTSTAR(INIT_J)=SIZE(DTSTAR,INIT_J); END DO; 
!      INIT_I=SIZE(SHAPE(EL)); M_EL=1; DO INIT_J=1,INIT_I; M_EL(INIT_J)=SIZE(EL,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ELCO)); M_ELCO=1; DO INIT_J=1,INIT_I; M_ELCO(INIT_J)=SIZE(ELCO,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(EVTREC)); M_EVTREC=1; DO INIT_J=1,INIT_I; M_EVTREC(INIT_J)=SIZE(EVTREC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(G3)); M_G3=1; DO INIT_J=1,INIT_I; M_G3(INIT_J)=SIZE(G3,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(H2ETA)); M_H2ETA=1; DO INIT_J=1,INIT_I; M_H2ETA(INIT_J)=SIZE(H2ETA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(HETA)); M_HETA=1; DO INIT_J=1,INIT_I; M_HETA(INIT_J)=SIZE(HETA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(HH)); M_HH=1; DO INIT_J=1,INIT_I; M_HH(INIT_J)=SIZE(HH,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(HR)); M_HR=1; DO INIT_J=1,INIT_I; M_HR(INIT_J)=SIZE(HR,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(I2AEA)); M_I2AEA=1; DO INIT_J=1,INIT_I; M_I2AEA(INIT_J)=SIZE(I2AEA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(I2DEA)); M_I2DEA=1; DO INIT_J=1,INIT_I; M_I2DEA(INIT_J)=SIZE(I2DEA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(I2REA)); M_I2REA=1; DO INIT_J=1,INIT_I; M_I2REA(INIT_J)=SIZE(I2REA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IA)); M_IA=1; DO INIT_J=1,INIT_I; M_IA(INIT_J)=SIZE(IA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IAA)); M_IAA=1; DO INIT_J=1,INIT_I; M_IAA(INIT_J)=SIZE(IAA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IAC)); M_IAC=1; DO INIT_J=1,INIT_I; M_IAC(INIT_J)=SIZE(IAC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IAEA)); M_IAEA=1; DO INIT_J=1,INIT_I; M_IAEA(INIT_J)=SIZE(IAEA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IAI)); M_IAI=1; DO INIT_J=1,INIT_I; M_IAI(INIT_J)=SIZE(IAI,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IAJ)); M_IAJ=1; DO INIT_J=1,INIT_I; M_IAJ(INIT_J)=SIZE(IAJ,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IAK)); M_IAK=1; DO INIT_J=1,INIT_I; M_IAK(INIT_J)=SIZE(IAK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IATT)); M_IATT=1; DO INIT_J=1,INIT_I; M_IATT(INIT_J)=SIZE(IATT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IBACK)); M_IBACK=1; DO INIT_J=1,INIT_I; M_IBACK(INIT_J)=SIZE(IBACK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ID)); M_ID=1; DO INIT_J=1,INIT_I; M_ID(INIT_J)=SIZE(ID,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IDA)); M_IDA=1; DO INIT_J=1,INIT_I; M_IDA(INIT_J)=SIZE(IDA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IDEA)); M_IDEA=1; DO INIT_J=1,INIT_I; M_IDEA(INIT_J)=SIZE(IDEA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IFORM)); M_IFORM=1; DO INIT_J=1,INIT_I; M_IFORM(INIT_J)=SIZE(IFORM,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IINST)); M_IINST=1; DO INIT_J=1,INIT_I; M_IINST(INIT_J)=SIZE(IINST,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ILAG)); M_ILAG=1; DO INIT_J=1,INIT_I; M_ILAG(INIT_J)=SIZE(ILAG,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ILINK)); M_ILINK=1; DO INIT_J=1,INIT_I; M_ILINK(INIT_J)=SIZE(ILINK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IMT)); M_IMT=1; DO INIT_J=1,INIT_I; M_IMT(INIT_J)=SIZE(IMT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IMTGG)); M_IMTGG=1; DO INIT_J=1,INIT_I; M_IMTGG(INIT_J)=SIZE(IMTGG,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(INEXT)); M_INEXT=1; DO INIT_J=1,INIT_I; M_INEXT(INIT_J)=SIZE(INEXT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(INRD)); M_INRD=1; DO INIT_J=1,INIT_I; M_INRD(INIT_J)=SIZE(INRD,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(INUM)); M_INUM=1; DO INIT_J=1,INIT_I; M_INUM(INIT_J)=SIZE(INUM,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IPC)); M_IPC=1; DO INIT_J=1,INIT_I; M_IPC(INIT_J)=SIZE(IPC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IPI)); M_IPI=1; DO INIT_J=1,INIT_I; M_IPI(INIT_J)=SIZE(IPI,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IPJ)); M_IPJ=1; DO INIT_J=1,INIT_I; M_IPJ(INIT_J)=SIZE(IPJ,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IPK)); M_IPK=1; DO INIT_J=1,INIT_I; M_IPK(INIT_J)=SIZE(IPK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IPOOL)); M_IPOOL=1; DO INIT_J=1,INIT_I; M_IPOOL(INIT_J)=SIZE(IPOOL,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IRA)); M_IRA=1; DO INIT_J=1,INIT_I; M_IRA(INIT_J)=SIZE(IRA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IREA)); M_IREA=1; DO INIT_J=1,INIT_I; M_IREA(INIT_J)=SIZE(IREA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IRR)); M_IRR=1; DO INIT_J=1,INIT_I; M_IRR(INIT_J)=SIZE(IRR,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(IS)); M_IS=1; DO INIT_J=1,INIT_I; M_IS(INIT_J)=SIZE(IS,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ISV)); M_ISV=1; DO INIT_J=1,INIT_I; M_ISV(INIT_J)=SIZE(ISV,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ITC)); M_ITC=1; DO INIT_J=1,INIT_I; M_ITC(INIT_J)=SIZE(ITC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ITI)); M_ITI=1; DO INIT_J=1,INIT_I; M_ITI(INIT_J)=SIZE(ITI,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ITK)); M_ITK=1; DO INIT_J=1,INIT_I; M_ITK(INIT_J)=SIZE(ITK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ITOTL)); M_ITOTL=1; DO INIT_J=1,INIT_I; M_ITOTL(INIT_J)=SIZE(ITOTL,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ITURN)); M_ITURN=1; DO INIT_J=1,INIT_I; M_ITURN(INIT_J)=SIZE(ITURN,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(LINK)); M_LINK=1; DO INIT_J=1,INIT_I; M_LINK(INIT_J)=SIZE(LINK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(MAP)); M_MAP=1; DO INIT_J=1,INIT_I; M_MAP(INIT_J)=SIZE(MAP,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(MAPINV)); M_MAPINV=1; DO INIT_J=1,INIT_I; M_MAPINV(INIT_J)=SIZE(MAPINV,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(MTIME)); M_MTIME=1; DO INIT_J=1,INIT_I; M_MTIME(INIT_J)=SIZE(MTIME,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(PP)); M_PP=1; DO INIT_J=1,INIT_I; M_PP(INIT_J)=SIZE(PP,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(PRMC)); M_PRMC=1; DO INIT_J=1,INIT_I; M_PRMC(INIT_J)=SIZE(PRMC,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(PRME)); M_PRME=1; DO INIT_J=1,INIT_I; M_PRME(INIT_J)=SIZE(PRME,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(PRMG)); M_PRMG=1; DO INIT_J=1,INIT_I; M_PRMG(INIT_J)=SIZE(PRMG,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(PRMT)); M_PRMT=1; DO INIT_J=1,INIT_I; M_PRMT(INIT_J)=SIZE(PRMT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(PWORKGM)); M_PWORKGM=1; DO INIT_J=1,INIT_I; M_PWORKGM(INIT_J)=SIZE(PWORKGM,INIT_J); END DO;
      INIT_I=SIZE(SHAPE(R)); M_R=1; DO INIT_J=1,INIT_I; M_R(INIT_J)=SIZE(R,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(R2E)); M_R2E=1; DO INIT_J=1,INIT_I; M_R2E(INIT_J)=SIZE(R2E,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(RE)); M_RE=1; DO INIT_J=1,INIT_I; M_RE(INIT_J)=SIZE(RE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(RHOE)); M_RHOE=1; DO INIT_J=1,INIT_I; M_RHOE(INIT_J)=SIZE(RHOE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(RHOE2)); M_RHOE2=1; DO INIT_J=1,INIT_I; M_RHOE2(INIT_J)=SIZE(RHOE2,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SDELE)); M_SDELE=1; DO INIT_J=1,INIT_I; M_SDELE(INIT_J)=SIZE(SDELE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SDELE2)); M_SDELE2=1; DO INIT_J=1,INIT_I; M_SDELE2(INIT_J)=SIZE(SDELE2,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SLEND)); M_SLEND=1; DO INIT_J=1,INIT_I; M_SLEND(INIT_J)=SIZE(SLEND,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SLST)); M_SLST=1; DO INIT_J=1,INIT_I; M_SLST(INIT_J)=SIZE(SLST,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SSAE)); M_SSAE=1; DO INIT_J=1,INIT_I; M_SSAE(INIT_J)=SIZE(SSAE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SSAE2)); M_SSAE2=1; DO INIT_J=1,INIT_I; M_SSAE2(INIT_J)=SIZE(SSAE2,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SSRE)); M_SSRE=1; DO INIT_J=1,INIT_I; M_SSRE(INIT_J)=SIZE(SSRE,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SSRE2)); M_SSRE2=1; DO INIT_J=1,INIT_I; M_SSRE2(INIT_J)=SIZE(SSRE2,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(SV)); M_SV=1; DO INIT_J=1,INIT_I; M_SV(INIT_J)=SIZE(SV,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(TESCO)); M_TESCO=1; DO INIT_J=1,INIT_I; M_TESCO(INIT_J)=SIZE(TESCO,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(THETAS)); M_THETAS=1; DO INIT_J=1,INIT_I; M_THETAS(INIT_J)=SIZE(THETAS,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(TT)); M_TT=1; DO INIT_J=1,INIT_I; M_TT(INIT_J)=SIZE(TT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(WORST)); M_WORST=1; DO INIT_J=1,INIT_I; M_WORST(INIT_J)=SIZE(WORST,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(WS11)); M_WS11=1; DO INIT_J=1,INIT_I; M_WS11(INIT_J)=SIZE(WS11,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(WS12)); M_WS12=1; DO INIT_J=1,INIT_I; M_WS12(INIT_J)=SIZE(WS12,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XAA)); M_XAA=1; DO INIT_J=1,INIT_I; M_XAA(INIT_J)=SIZE(XAA,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XIAI)); M_XIAI=1; DO INIT_J=1,INIT_I; M_XIAI(INIT_J)=SIZE(XIAI,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XIAJ)); M_XIAJ=1; DO INIT_J=1,INIT_I; M_XIAJ(INIT_J)=SIZE(XIAJ,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XIAK)); M_XIAK=1; DO INIT_J=1,INIT_I; M_XIAK(INIT_J)=SIZE(XIAK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XIPI)); M_XIPI=1; DO INIT_J=1,INIT_I; M_XIPI(INIT_J)=SIZE(XIPI,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XIPJ)); M_XIPJ=1; DO INIT_J=1,INIT_I; M_XIPJ(INIT_J)=SIZE(XIPJ,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XIPK)); M_XIPK=1; DO INIT_J=1,INIT_I; M_XIPK(INIT_J)=SIZE(XIPK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XITI)); M_XITI=1; DO INIT_J=1,INIT_I; M_XITI(INIT_J)=SIZE(XITI,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XITK)); M_XITK=1; DO INIT_J=1,INIT_I; M_XITK(INIT_J)=SIZE(XITK,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XPP)); M_XPP=1; DO INIT_J=1,INIT_I; M_XPP(INIT_J)=SIZE(XPP,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(XTT)); M_XTT=1; DO INIT_J=1,INIT_I; M_XTT(INIT_J)=SIZE(XTT,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ZS11)); M_ZS11=1; DO INIT_J=1,INIT_I; M_ZS11(INIT_J)=SIZE(ZS11,INIT_J); END DO; 
      INIT_I=SIZE(SHAPE(ZS12)); M_ZS12=1; DO INIT_J=1,INIT_I; M_ZS12(INIT_J)=SIZE(ZS12,INIT_J); END DO; 
!
! Vector PR dimensions
      M_EL(1)=SIZE(EL,1) 
      M_IDEFA=SIZE(IDEFA,1) 
      M_IDEFD=SIZE(IDEFD,1) 
      M_TQ=SIZE(TQ,1) 
!
  999 RETURN
!      
      END SUBROUTINE PRDIMENSION_SETUP
