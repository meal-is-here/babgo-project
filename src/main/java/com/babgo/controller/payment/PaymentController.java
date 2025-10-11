package com.babgo.controller.payment;

import com.babgo.application.payment.PaymentFacade;
import com.babgo.application.payment.PaymentInfo;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping
    public ApiResponse<PaymentResponse.Ready> ready(
            @RequestBody PaymentRequest.Ready request
    ){
        //TODO 임시 값
        UUID userid = UUID.randomUUID();
        PaymentInfo.Ready input = PaymentInfo.Ready.from(userid, request);
        PaymentInfo.ReadyResult output = paymentFacade.ready(input);
        PaymentResponse.Ready response =  PaymentResponse.Ready.from(output);
        return ApiResponse.success("결제 요청이 정상적으로 접수되었습니다.", response);
    }
}
