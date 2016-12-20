package socket.java;

class Response {
    enum Status {
        Success(0), Failed(1);

        private int code_ = 0;

        private Status(int code) {
            this.code_ = code;
        }
    }

    long reqID;
    byte[] data;
    Status status;

    static public Response defaultResponse() {
        Response res = new Response();
        res.reqID = 0;
        res.status = Status.Success;
        res.data = null;
        return res;
    }
}
