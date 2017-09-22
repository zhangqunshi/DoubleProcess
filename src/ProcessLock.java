import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ProcessLock {

    public static void main(String[] args) throws IOException {
        System.out.println("start");
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);

        String pid = name.split("@")[0];
        System.out.println("Pid is:" + pid);
        File pidFile = new File("pid." + pid);
        if (!pidFile.exists()) {
            pidFile.createNewFile();
        }

        String lockFilename = "process.lock";
        FileChannel channel = null;
        FileLock lock = null;
        try {
            File lockFile = new File(lockFilename);
            if (!lockFile.exists()) {
                if (!lockFile.createNewFile()) {
                    throw new Exception("Cannot create lock file: " + lockFile.getAbsolutePath());
                }
            }

            channel = new RandomAccessFile(lockFile, "rw").getChannel();
            System.out.println("--34---");
            lock = channel.lock();
            System.out.println("--36---");
            if (lock != null) {
                ProcessBuilder pb =
                        new ProcessBuilder("java", "ProcessLock");
                pb.redirectErrorStream(true);
                File pidLogFile = new File("pid_" + pid + ".log");
//                pb.redirectError(pidLogFile);
                pb.redirectOutput(pidLogFile);
                Process p = pb.start();

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            boolean alive = p.isAlive();

                            if (!alive) {
                                System.out.println(pid + " + child is dead");
                            } else {
                                System.out.println(pid + " child is alive");
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                };
                t.setDaemon(true);
                t.start();

//                Field pidField = p.getClass().getDeclaredField("pid");
//                pidField.setAccessible(true);
//                Object childPid = pidField.get(p);
//                System.out.println("start child pid".concat(childPid.toString()));


//                InputStream is = p.getInputStream(); // 获得输入流
//                InputStreamReader isr = new InputStreamReader(is);// 创建输入读流，编码方式为GBK
//                BufferedReader br = new BufferedReader(isr); // 创建读缓冲对象
//                String line;
//                while ((line = br.readLine()) != null) {// 循环读取数据
//                    System.out.println(line);
//                }

                System.out.println("sleep 5 sec");
                Thread.sleep(50000);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
