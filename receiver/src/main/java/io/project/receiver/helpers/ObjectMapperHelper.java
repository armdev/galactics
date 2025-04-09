package io.project.receiver.helpers;

import io.project.receiver.domain.PaymentEvent;
import io.project.receiver.events.AccountEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

public class ObjectMapperHelper {

    // From entity to response
    public static PaymentEvent fromEventToEntity(AccountEvent input) {
        PaymentEvent output = new PaymentEvent();
        try {
            BeanUtils.copyProperties(input, output);
        } catch (BeansException e) {
            throw new RuntimeException("Error creating output from input", e);
        }
        return output;
    }

}
