package cn.keepbx.jpom.socket;

import cn.jiangzeyin.common.DefaultSystemLog;
import cn.keepbx.jpom.util.KeyLock;

import javax.websocket.Session;
import java.io.IOException;

/**
 * socket 回话对象
 *
 * @author jiangzeyin
 * @date 2018/9/29
 */
public class SocketSession {
    /**
     * 锁
     */
    private static final KeyLock<String> LOCK = new KeyLock<>();
    /**
     * 错误尝试次数
     */
    private static final int ERROR_TRY_COUNT = 10;

    private TailLogThread thread;
    private Session session;


    public SocketSession(Session session) {
        this.session = session;
    }

    public TailLogThread getThread() {
        return thread;
    }

    public void setThread(TailLogThread thread) {
        this.thread = thread;
    }

    /**
     * 发送消息
     *
     * @param msg 消息
     */
    public void sendMsg(String msg) throws IOException {
        if (session == null) {
            return;
        }
        DefaultSystemLog.LOG().info(msg);
        send(session, msg);
    }

    /**
     * 发送消息
     *
     * @param session 回话对象
     * @param msg     消息
     * @throws IOException 异常
     */
    public static void send(final Session session, String msg) throws IOException {
        if (msg == null) {
            return;
        }
        if (!session.isOpen()) {
            return;
        }
        for (int i = 1; i <= ERROR_TRY_COUNT; i++) {
            try {
                LOCK.lock(session.getId());
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                DefaultSystemLog.ERROR().error("发送消息失败:" + i, e);
                if (i == ERROR_TRY_COUNT) {
                    throw e;
                } else {
                    // 休眠一秒
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }
            } finally {
                LOCK.unlock(session.getId());
            }
            break;
        }
    }
}
