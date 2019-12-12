package task.message;

enum MegOperation {

    /**
     * ADD 添加
     * DEL 删除
     * DEL_ALL 删除所有
     */
    ADD(), DEL(), DEL_ALL();
    /*** 消息接收者*/
    private MessageCourier courier = null;

    public MegOperation setCourier(MessageCourier courier) {
        this.courier = courier;
        return this;
    }

    public MessageCourier getCourier() {
        return courier;
    }
}
