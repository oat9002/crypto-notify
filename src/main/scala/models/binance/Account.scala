package models.binance

case class Account(
                    coin: String,
                    depositAllEnable: Boolean,
                    free: BigDecimal,
                    freeze: BigDecimal,
                    ipoable: BigDecimal,
                    ipoing: BigDecimal,
                    isLegalMoney: Boolean,
                    locked: BigDecimal,
                    name: String,
                    networkList: List[NetWork],
                    storage: BigDecimal,
                    trading: Boolean,
                    withdrawAllEnable: Boolean,
                    withdrawing: BigDecimal
                  )

case class NetWork(
                  addressRegex: String,
                  coin: String,
                  depositDesc: String,
                  isDefault: Boolean,
                  memoRegex: Boolean,
                  minComfirm: Int,
                  name: String,
                  network: String,
                  resetAddressStatus: Boolean,
                  specialTips: String,
                  unLockConfirm: Int,
                  withdrawDesc: String,
                  withdrawFee: BigDecimal,
                  withdrawIntegerMultiple: BigDecimal,
                  withdrawMax: BigDecimal,
                  withdrawMin: BigDecimal,
                  sameAddress: Boolean
                  )



