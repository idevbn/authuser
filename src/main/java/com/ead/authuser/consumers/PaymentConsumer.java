package com.ead.authuser.consumers;

import com.ead.authuser.dtos.PaymentEventDTO;
import com.ead.authuser.enums.PaymentControl;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private final UserService userService;
    private final RoleService roleService;

    public PaymentConsumer(final UserService userService,
                           final RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${ead.broker.queue.paymentEventQueue.name}", durable = "true"),
            exchange = @Exchange(
                    value = "${ead.broker.exchange.paymentEvent}",
                    type = ExchangeTypes.FANOUT,
                    ignoreDeclarationExceptions = "true"
            )
    ))
    public void listenPaymentEvent(@Payload final PaymentEventDTO paymentEventDTO) {
        final UserModel userModel = this.userService.findById(paymentEventDTO.getUserId()).get();
        final RoleModel roleModel = this.roleService.findByRoleName(RoleType.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        switch (PaymentControl.valueOf(paymentEventDTO.getPaymentControl())) {
            case EFFECTED:
                if (userModel.getUserType().equals(UserType.USER)) {
                    userModel.setUserType(UserType.STUDENT);
                    userModel.getRoles().add(roleModel);
                    this.userService.updateUser(userModel);
                }
                break;

            case REFUSED:
                if (userModel.getUserType().equals(UserType.STUDENT)) {
                    userModel.setUserType(UserType.USER);
                    userModel.getRoles().add(roleModel);
                    this.userService.updateUser(userModel);
                }
                break;

            case ERROR:
                System.out.println("Payment with ERROR");
        }

    }

}
