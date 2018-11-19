package task.message;

import task.message.joggle.IMsgCourier;

enum MegOperation {

    /**
     * ADD 添加
     * DEL 删除
     * DEL_ALL 删除所有
     */
    ADD(), DEL(), DEL_ALL();
    /*** 消息接收者*/
    private IMsgCourier courier = null;

    public MegOperation setCourier(IMsgCourier courier) {
        this.courier = courier;
        return this;
    }

    public IMsgCourier getCourier() {
        return courier;
    }
}
