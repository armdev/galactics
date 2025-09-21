package io.project.sender.events;

import com.github.f4b6a3.uuid.UuidCreator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AccountEventDataGenerator {

    private static final String[] CURRENCIES = {"AMD", "USD", "EURO", "RUB"};

    public static List<AccountEvent> generateEvents() {
        List<AccountEvent> events = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            AccountEvent event = new AccountEvent();
            event.setId(UuidCreator.getTimeOrderedEpoch().toString());
            event.setClientId(UuidCreator.getTimeOrderedEpoch().toString());
            // event.setClientAccountNumber("80344895198");
            event.setClientAccountNumber(generateAccountNumber());
            event.setNotificationId(UuidCreator.getTimeOrderedEpoch().toString());
            event.setTimestamp(System.currentTimeMillis());
            event.setCurrency(CURRENCIES[random.nextInt(CURRENCIES.length)]);
            event.setReceivedAmount(BigDecimal.valueOf(random.nextDouble() * 10000));
            event.setBeneficiary(TransactionBeneficiary.randomEvent().name());
            event.setTransactionId(UuidCreator.getTimeOrderedEpoch().toString());
            event.setDetails("Details for event " + i);
            event.setStatus("NEW");
            event.setTransactionCode(TransactionCode.randomEvent().name());
            events.add(event);
        }

        return events;
    }

    enum TransactionCode {
        CrPayOrd, DbPayFor, DbPayOrd, KasPrOrd, OCIPay, PercCalc;

        private static final Random PRNG = new Random();

        public static TransactionCode randomEvent() {
            TransactionCode[] directions = values();
            return directions[PRNG.nextInt(directions.length)];
        }
    }

    enum TransactionBeneficiary {
        ArdshinBank, AmeriaBank, Inecobank, HSBD_MALTA, CONVERCE_BANK, EvocaBank;

        private static final Random PRNG = new Random();

        public static TransactionBeneficiary randomEvent() {
            TransactionBeneficiary[] directions = values();
            return directions[PRNG.nextInt(directions.length)];
        }
    }

    private static String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder("401");
        for (int i = 0; i < 7; i++) {
            accountNumber.append(random.nextInt(10));
        }

        return accountNumber.toString();
    }
}
