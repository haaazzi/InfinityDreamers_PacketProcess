package com.infinitydreamers.node;

import com.infinitydreamers.message.Message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DebugNode extends InputOutputNode {

    public DebugNode(String name) {
        super(name);
    }

    @Override
    public void process() {
        if ((getInputWire(0) != null) && getInputWire(0).hasMessage()) {
            Message message = getInputWire(0).get();
            String failString = "";
            if (message.isFlag()) {
                successCount++;

                log.debug("전체 : " + (successCount + failCount) + " / 성공 : " + successCount + " / 실패 : " + failCount);
            } else {
                failCount++;
                failString = message.getJson().getString("fail");

                log.debug("전체 : " + (successCount + failCount) + " / 성공 : " + successCount + " / 실패 : " + failCount);
                log.debug(failString);
            }
        }
    }
}
