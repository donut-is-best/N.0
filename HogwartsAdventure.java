import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HogwartsAdventure extends JFrame {
    // 游戏核心数值
    private int magicPoints = 50;
    private int timeLeft = 72;
    private int healthPoints = 100;
    private boolean hasWand = false;
    private boolean hasMagicCoin = false;
    private boolean hasGillyweed = true;
    private boolean hasSnakeDagger = false;
    private boolean hasHufflepuffCup = false;
    private boolean hasAshesFragment = false;
    private boolean bribedSnape = false;
    private boolean completedBlackLake = false;
    private boolean foughtNagini = false;
    private boolean destroyedDiary = false;
    private boolean usedStealthSpell = false;
    private boolean[] achievements = new boolean[3]; // 策略大师、魂器猎人、历史改写者

    // 剧情状态
    private int currentChapter = 0;
    private List<String> storyHistory = new ArrayList<>();
    private boolean isReadyForNextChapter = false; // 标记是否准备好进入下一章节
    private String currentChapterText = ""; // 当前章节的剧情文本


    // UI组件
    private JLabel magicPointsLabel;
    private JLabel timeLeftLabel;
    private JLabel healthPointsLabel;
    private JLabel currentTaskLabel;
    private JTextArea storyArea;
    private JPanel optionsPanel;
    private JButton saveButton;
    private JButton loadButton;
    private JPanel statusPanel;
    private JPanel mainPanel;
    private JPanel bottomPanel;
    private JPanel headerPanel;
    private JLabel titleLabel;
    private JScrollPane storyScrollPane; 

    // 剧情数据
    private Map<Integer, Chapter> chapters = new HashMap<>();

    // 组件样式
    private Color backgroundColor = new Color(25, 25, 45);
    private Color panelColor = new Color(50, 50, 80);
    private Color textColor = new Color(255, 230, 180);
    private Color buttonColor = new Color(100, 80, 50);
    private Color buttonHoverColor = new Color(150, 120, 80);
    private Color borderColor = new Color(200, 160, 80);

    public HogwartsAdventure() {
        initUI();
        loadChapters();
        showChapter(0); // 显示序章
        setupStoryAreaListener(); // 设置剧情区域点击监听

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(""));
            if (icon != null) {
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.out.println("无法加载窗口图标: " + e.getMessage());
        }
    }
    
    //黑湖部分拼写小游戏
    private boolean spellCastingGame() {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        
        // 咒语列表
        List<String> spells = Arrays.asList(
            "AquaEterna",     // 永恒之水
            "LumosMaxima",    // 荧光闪烁
            "Nox",            // 诺克斯（熄灯咒）
            "WingardiumLeviosa", // 悬浮咒
            "ExpectoPatronum" // 呼神护卫
        );
        
        Random random = new Random();
        String correctSpell = spells.get(random.nextInt(spells.size()));
        
        AtomicInteger timeLimit = new AtomicInteger(15); 
        
        JTextField inputField = new JTextField();
        JLabel timerLabel = new JLabel("剩余时间: " + timeLimit + " 秒");
        timerLabel.setForeground(Color.RED);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        Font chineseFont = new Font("SimHei", Font.PLAIN, 14);
        
        JLabel instructionLabel = new JLabel("请在限时内拼写咒语 " + correctSpell + ": ");
        instructionLabel.setFont(chineseFont.deriveFont(Font.BOLD, 16f));
        panel.add(instructionLabel, BorderLayout.NORTH);
        
        inputField.setFont(chineseFont.deriveFont(16f));
        panel.add(inputField, BorderLayout.CENTER);
        
        timerLabel.setFont(chineseFont.deriveFont(Font.BOLD, 16f));
        panel.add(timerLabel, BorderLayout.SOUTH);
        
        javax.swing.Timer timer = new javax.swing.Timer(1000, new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentTime = timeLimit.decrementAndGet();
                timerLabel.setText("剩余时间: " + currentTime + " 秒");
                
                if (currentTime <= 3) {
                    timerLabel.setForeground(currentTime % 2 == 0 ? Color.RED : Color.ORANGE);
                }
                
                if (currentTime <= 0) {
                    ((javax.swing.Timer) e.getSource()).stop();
                    showMessageDialog("时间到！游戏失败" , "游戏结束", JOptionPane.ERROR_MESSAGE);
                    inputField.setEnabled(false);
                }
            }
        });
        timer.start();

        int result = JOptionPane.showConfirmDialog(
            null, 
            panel, 
            "限时拼写咒语小游戏", 
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        timer.stop();

        if (result == JOptionPane.OK_OPTION) {
            String userInput = inputField.getText().trim();
            boolean isCorrect = userInput.equals(correctSpell);
            
            String message = isCorrect ? 
                "恭喜你，拼写正确！" : 
                "拼写错误！正确咒语是：" + correctSpell;
                
            showMessageDialog(
                message, 
                isCorrect ? "成功" : "失败", 
                isCorrect ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
            );
            
            return isCorrect;
        }
        return false;
    }

    // 自定义消息对话框，确保中文显示
    private void showMessageDialog(String message, String title, int messageType) {
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(null);
        
        // 使用相同的中文字体
        Font chineseFont = new Font("SimHei", Font.PLAIN, 14);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setFont(chineseFont);
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(chineseFont);
        panel.add(messageLabel, BorderLayout.CENTER);
        
        JButton okButton = new JButton("确定");
        okButton.setFont(chineseFont);
        okButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void initUI() {
        setTitle("霍格沃茨：倒计时抉择");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建主面板
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);

        // 创建顶部标题栏
        headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(panelColor);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, borderColor));

        titleLabel = new JLabel("霍格沃茨：倒计时抉择");
        titleLabel.setForeground(textColor);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // 添加标题效果
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Font font = titleLabel.getFont();
                Map attributes = font.getAttributes();
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                titleLabel.setFont(font.deriveFont(attributes));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Font font = titleLabel.getFont();
                Map attributes = font.getAttributes();
                attributes.put(TextAttribute.UNDERLINE, -1);
                titleLabel.setFont(font.deriveFont(attributes));
            }
        });

        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 状态栏
        statusPanel = new JPanel(new GridLayout(1, 4, 10, 5));
        statusPanel.setBackground(panelColor);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 2, 0, borderColor),
                new EmptyBorder(5, 10, 5, 10)
        ));

        magicPointsLabel = createStatusLabel("魔力值: " + magicPoints);
        healthPointsLabel = createStatusLabel("生命值: " + healthPoints);
        currentTaskLabel = createStatusLabel("当前任务: 序章 - 蓝光裂隙");

        statusPanel.add(magicPointsLabel);
        statusPanel.add(healthPointsLabel);
        statusPanel.add(currentTaskLabel);

        mainPanel.add(statusPanel, BorderLayout.PAGE_START);

        // 剧情显示区域
        JPanel storyContainer = new JPanel(new BorderLayout());
        storyContainer.setBackground(backgroundColor);
        storyContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        storyArea = new JTextArea();
        storyArea.setFont(new Font("Serif", Font.PLAIN, 16));
        storyArea.setEditable(false);
        storyArea.setLineWrap(true);
        storyArea.setWrapStyleWord(true);
        storyArea.setForeground(new Color(50, 30, 0));
        storyArea.setBackground(new Color(250, 230, 190));
        storyArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 140, 80), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // 初始化 storyScrollPane 并设置为成员变量
        storyScrollPane = new JScrollPane(storyArea);
        storyScrollPane.setBorder(null);
        storyScrollPane.getViewport().setBackground(backgroundColor);
        storyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        storyScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        storyContainer.add(storyScrollPane, BorderLayout.CENTER);

        // 选项面板
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(backgroundColor);
        optionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 主内容区域布局
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(backgroundColor);
        contentPanel.add(storyContainer, BorderLayout.CENTER);
        contentPanel.add(optionsPanel, BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 底部功能区
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(panelColor);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, borderColor));

        saveButton = createStyledButton("存档");
        loadButton = createStyledButton("读档");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveGame();
            }
        });

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadGame();
            }
        });
        
        
        

        bottomPanel.add(saveButton);
        bottomPanel.add(loadButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
    
    private void restartGame() {
        // 重新初始化游戏核心数值
        magicPoints = 50;
        timeLeft = 72;
        healthPoints = 100;
        hasWand = false;
        hasMagicCoin = false;
        hasGillyweed = true;
        hasSnakeDagger = false;
        hasHufflepuffCup = false;
        hasAshesFragment = false;
        bribedSnape = false;
        completedBlackLake = false;
        foughtNagini = false;
        destroyedDiary = false;
        usedStealthSpell = false;
        Arrays.fill(achievements, false);

        // 重新初始化剧情状态
        currentChapter = 0;
        storyHistory.clear();
        isReadyForNextChapter = false;
        currentChapterText = "";

        // 更新状态栏
        updateStatus();

        // 显示序章
        showChapter(0);

        // 清空选项面板
        optionsPanel.removeAll();
        optionsPanel.revalidate();
        optionsPanel.repaint();

        // 清空剧情显示区域
        storyArea.setText("");
    }

    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(textColor);
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(textColor);
        button.setBackground(buttonColor);
        button.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setMaximumSize(new Dimension(120, 40));

        // 添加按钮悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(buttonHoverColor);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(buttonColor);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }
    
    private void setupStoryAreaListener() {
        // 为剧情区域添加鼠标点击监听
        storyArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 如果准备好进入下一章节，则显示下一章节
                if (isReadyForNextChapter) {
                    isReadyForNextChapter = false;
                    showChapter(currentChapter);
                }
            }
        });
    }


    private void loadChapters() {
    	//0章
    	Chapter newPrologue = new Chapter(
    		    "我的鞋底碾过九又四分之三站台的碎石，血腥味突然如潮水般漫上来。原本应该挤满巫师家庭的站台空荡荡的，黑色蒸汽从霍格沃茨特快的烟囱里滚滚涌出，在半空凝结成扭曲的人脸轮廓。那些模糊的面容似乎在无声尖叫，每一张都带着我从未在原著里见过的惊恐与绝望。\n" +"\n"+
    		    "“欢迎来到‘霍格沃兹:倒计时抉择’副本。” 机械音像是从喉咙里卡着铁锈发出的，我猛地转身，只看见自己的呼吸在空中凝成一行猩红字迹。字迹未散，我的手腕突然传来灼烧般的剧痛，银色契约纹路如同活物般爬上皮肤，末端的沙漏正以肉眼可见的速度流逝。\n" +"\n"+
    		    "蒸汽中传来窸窸窣窣的脚步声，像是有人拖着什么重物在移动。我下意识摸向口袋，触到一个物件 —— 不知何时出现的魔杖正在发烫。当指尖拂过，周围的空气突然凝固，所有蒸汽悬停在空中，组成了一幅幅诡异的画面：密室门前阴冷的笑，魔药倒进坩埚时，坩埚里映出的却是伏地魔的脸。\n" +"\n"+
    		    "天空中浮现出细小的金字：魂器之一，藏于禁忌之处。就在这时，一声悠长的汽笛划破死寂，特快列车的车门自动打开，幽绿的光从门缝里倾泻而出。一个披着长袍的身影立在阴影中，魔杖顶端的光芒明明灭灭，我却看不清那张藏在兜帽下的脸。当他向我伸出手时，袖口滑落露出的，是和我手腕上一模一样的银色契约纹路。", 
    		    "确认进入游戏"
    		);
        newPrologue.addOption("确认", "正式进入游戏剧情", new OptionAction() {
            @Override
            public void execute() {
                String history = "眼前银光一闪，我已出现在霍格沃兹城堡外";
                storyHistory.add(history);
                displayHistoryAndWait(history, 1);
            }
        });
        chapters.put(0, newPrologue);
        
        // 序章：蓝光裂隙
        Chapter prologue = new Chapter("冬日的霍格沃茨城堡外，一片死寂，厚厚的积雪覆盖着大地，放眼望去，白茫茫一片。寒风凛冽，如刀子般刮过面庞。摄魂怪低沉的鸣叫声从四面八方传来，它们那如幽灵般的身影在雪地中若隐若现，所到之处，寒意愈发刺骨，周围的空气仿佛都被冻结。\r\n"
        		+ "\r\n"
        		+ "你的魔杖不知何时遗落在了雪地里，杖身半掩在雪中，杖尖闪烁着微弱的蓝光，在这冰天雪地中显得格外突兀，角度偏转有什么东西晃了你的眼，是一枚银币。摄魂怪好像发现了什么，开始往这边游荡，此时，你必须做出初始抉择，每一秒都如同被拉长，生死命运悬于一线。\r\n"
        		+ "", "完成初始抉择。");
        prologue.addOption("冲向魔杖", "需消耗10魔力释放守护神咒（失败即死）", new OptionAction() {
            @Override
            public void execute() {
                if (magicPoints >= 10) {
                    magicPoints -= 10;
                    hasWand = true;
                    String history = "你心中只有一个念头，那就是夺回自己的魔杖。不顾周身的寒冷与危险，你双脚用力一蹬，朝着魔杖的方向冲去。积雪没过脚踝，每一步都迈得艰难无比，寒风呼啸着灌进衣领，冰冷刺骨。\r\n"
                    		+ "\r\n"
                    		+ "当你终于握住魔杖的那一刻，一股熟悉的力量瞬间传遍全身。然而，还未等你松一口气，一只摄魂怪已经张牙舞爪地向你扑来，它那黑洞洞的嘴仿佛要将你吞噬。你深知此时不能慌乱，集中精神，调动体内的魔力，大声喊道：“守护神咒！”瞬间，一道银色的光芒从魔杖尖端射出，化作一只威风凛凛的守护神，与摄魂怪展开了激烈的对抗。"
                    		+ "\r\n"
                    		+ "这一战，你消耗了10点魔力，但成功赶跑了摄魂怪，同时获得了「魔杖」，它将为你接下来的冒险提供基础攻击力+20的助力 。\r\n";
                    storyHistory.add(history);
                    displayHistoryAndWait(history, 2);
                } else {
                    showEnding("你魔力不足，无法释放守护神咒，被摄魂怪袭击...", "BE：被摄魂怪吞噬");
                }
                updateStatus();
            }
        });

        prologue.addOption("捡起银币", "获得「魔法银币」（可用于贿赂）", new OptionAction() {
            @Override
            public void execute() {
                hasMagicCoin = true;
                String history="在慌乱之中，你瞥见脚边有一枚散发着微光的魔法银币。几乎是下意识地，你弯腰将它捡起。就在你拿起银币的瞬间，一阵阴寒的风刮过，你抬头望去，摄魂怪的身影愈发逼近。你意识到，此刻去拿魔杖已经来不及了，为了躲避摄魂怪，你只能转身拼命朝着城堡的方向跑去。\r\n"
                		+ "\r\n"
                		+ "成功进入城堡后，你稍稍松了口气。看着手中的「魔法银币」，你心想或许它在之后会派上用场，比如用来贿赂某些人。但你也清楚，因为错过魔杖，后续遇到战斗时，难度将会增加 。\r\n"
                		+ "";
		        storyHistory.add(history);
		        displayHistoryAndWait(history, 2);

            }
        });

        prologue.addOption("躲进阴影", "安全进入城堡，但丢失「魔杖线索」", new OptionAction() {
            @Override
            public void execute() {
            	 String history ="你敏锐地察觉到周围的危险，深知自己此刻的实力可能无法与摄魂怪正面抗衡。于是，你迅速环顾四周，发现不远处有一处阴影地带，那里光线昏暗，或许能成为暂时的庇护所。你小心翼翼地朝着阴影处移动，尽量不发出声响，每一步都踏得极为谨慎，生怕惊动了那些可怕的摄魂怪。\r\n"
                		+ "\r\n"
                		+ "终于，你成功躲进了阴影中。摄魂怪在附近盘旋了几圈，似乎没有察觉到你的踪迹，渐渐飞远。待它们彻底消失后，你才敢从阴影中走出，进入城堡。\r\n"
                		+ "";
            	 storyHistory.add(history);
                 displayHistoryAndWait(history, 2);

            }
        });
        chapters.put(1, prologue);

        // 第一幕：礼堂疑云
        Chapter act1 = new Chapter("踏入霍格沃茨礼堂，原本庄严肃穆的氛围此刻却被一股诡异所笼罩。分院帽突然发出尖锐的尖叫声，声音在礼堂内回荡，令人毛骨悚然。麦格教授一脸凝重地走到你面前，递给你一张地图，说道：“时间紧迫，按地图前往「猫头鹰棚屋」，那里或许藏着解开这一切谜团的线索。”你接过地图，发现上面的路径错综复杂，有些地方甚至模糊不清，仿佛在暗示着前方的道路充满未知与危险。\r\n"
        		+ "\r\n"
        		+ "到达「猫头鹰棚屋」。\r\n"
        		+ "\r\n"
        		+ "站在「猫头鹰棚屋」前，看着眼前的门，你又摸了摸身上，自己只带了一根铁丝和之前在霍格莫德村购买的「巧克力蛙」。\r\n"
        		+ ""
        		+ "", "请完成抉择");
        act1.addOption("开锁", "触发警报，需用银币贿赂斯内普", new OptionAction() {
            @Override
            public void execute() {
            	hasGillyweed=true;
                if (hasMagicCoin) {
                	String history = "你仔细研究了地图，找到了猫头鹰棚屋的位置，一路小心翼翼地来到门前。看着紧闭的门，你决定尝试开锁。你从口袋里掏出一根细长的铁丝，这是你平日里为了应对各种情况而准备的小工具。你将铁丝插入锁孔，轻轻转动，眼睛紧紧盯着锁芯，耳朵仔细聆听着锁内传来的细微声响。\r\n"
                			+ "\r\n"
                			+ "就在你快要成功打开锁的时候，突然，一阵尖锐的警报声响起。你心中暗叫不好，还没等你做出反应，斯内普教授那阴沉的身影就出现在了走廊尽头。他迈着缓慢而沉重的步伐向你走来，每一步都仿佛踏在你的心上。“未经允许，私自闯入，你知道这是什么后果吗？”斯内普冷冷地说道，眼神中透露出一丝危险的气息。\r\n"
                			+ "\r\n"
                			+ "你心中慌乱，但突然想起之前捡到的「魔法银币」。你急忙掏出银币，双手递向斯内普，说道：“教授，我……我只是太着急了，这是一点小意思，希望您能通融通融。”斯内普看着你手中的银币，犹豫了一下，最终伸手接了过去。“下次再让我发现，就没这么简单了。”说完，他转身离去。你松了一口气，打开门进入棚屋。在屋内的一个角落里，你发现了「黑湖线索」和可能是斯内普遗漏的「鳃囊草」。\r\n"
                			+ "\r\n"
                			+ "当你攥着「黑湖线索」从猫头鹰棚屋退出来时，羊皮纸上的墨痕突然泛起幽蓝荧光。那些记载着水草纹路和气泡符号的文字，竟在烛光下重组为一行新的提示：「魂器的倒影藏在有求必应屋的裂隙里 —— 唯有蛇形之刃能划破水面。」你这才意识到，麦格教授给的地图背面还印着半枚蛇形徽章，而黑湖线索的末端正画着有求必应屋的螺旋楼梯。魔杖尖划过墙壁的瞬间，潮湿的石砖突然向内凹陷，露出通往禁忌房间的暗门，门后飘来的霉味里混杂着一丝蛇信子的腥气。\r\n"
                			+ " ";
                	storyHistory.add(history);
                	bribedSnape = true;
                	displayHistoryAndWait(history, 3);
                } else {
                    showEnding("你仔细研究了地图，找到了猫头鹰棚屋的位置，一路小心翼翼地来到门前。看着紧闭的门，你决定尝试开锁。你从口袋里掏出一根细长的铁丝，这是你平日里为了应对各种情况而准备的小工具。你将铁丝插入锁孔，轻轻转动，眼睛紧紧盯着锁芯，耳朵仔细聆听着锁内传来的细微声响。\r\n"
                    		+ "\r\n"
                    		+ "铁丝在锁孔里卡到最后一个弹片时，警报声像被踩住尾巴的猫般骤然撕裂寂静。我手指猛地僵住，冰凉的铁丝差点从汗湿的掌心滑落。走廊尽头的油灯突然全部亮起，斯内普教授的黑袍如蝙蝠翅膀般翻飞着掠过转角，魔杖尖泛着幽蓝的光。\r\n"
                    		+ "\r\n"
                    		+ "“在我的地盘玩小偷游戏？”他的声音裹着冰碴，魔杖轻轻一挥，我整个人就被无形的力量拽到他面前。后背重重撞在石墙上，散落的铁丝叮叮当当滚了一地。我张了张嘴，喉咙却像被魔法胶封住，那些提前编好的借口全变成了齑粉。\r\n"
                    		+ "\r\n"
                    		+ "教授的黑靴碾过铁丝，火星迸溅在我颤抖的鞋尖。“夜游、破坏防护咒、意图盗窃……”他慢条斯理地数着罪状，魔杖尖已经抵住我的下巴，“格兰芬多扣五十分——不，是五十分乘以三。”\r\n"
                    		+ "\r\n"
                    		+ "挣扎着被押往禁闭室时，我绝望地盯着猫头鹰棚屋紧闭的门。方才指尖还残留着锁芯转动的微妙触感，此刻却只能听见自己越来越急促的心跳，混着斯内普教授阴森的脚步声，在空荡的走廊里回响。\r\n"
                    		+ "", "BE：抓进禁闭室");
                }
                updateStatus();
            }
        });

        act1.addOption("观察后窗", "发现斯内普焚烧信件，获得「灰烬残片」", new OptionAction() {
            @Override
            public void execute() {
                hasAshesFragment = true;
                hasGillyweed=true;
                String history = "你没有选择直接开锁，而是决定先观察一下周围的环境。你绕到棚屋的后窗，透过窗户向里望去。屋内光线昏暗，隐隐约约能看到一些猫头鹰的身影在笼子里扑腾。就在你准备进一步观察时，你注意到一个熟悉的身影——斯内普教授。\r\n"
                		+ "\r\n"
                		+ "他正站在一张桌子前，手中拿着一叠信件，脸上的表情极为凝重。只见他将信件放在烛火上，看着信件被火焰一点点吞噬，化为灰烬。你心中充满疑惑，斯内普教授为什么要烧掉这些信件？你决定寻找机会一探究竟。\r\n"
                		+ "\r\n"
                		+ "经过一番寻找，你在窗台下发现了一些灰烬残片和可能是斯内普遗留的「鳃囊草」，你小心翼翼地将这些残片收集起来，心想这或许就是解开谜团的关键。\r\n"
                		+ "\r\n"
                		+ "你将「灰烬残片」拼在烛火前，烧焦的羊皮纸忽然渗出血色文字：「日记本的幻影在有求必应屋等待吞噬 —— 用蛇形匕首刺穿它的谎言。」残片边缘残留的蜡封印记正是斯莱特林的蛇徽，而那些被斯内普烧掉的信件，信纸纤维里分明混着有求必应屋特有的银箔纹路。当你沿着地图标记的密道走向走廊尽头时，墙壁突然裂开一道缝隙，露出布满蛛网的螺旋楼梯。台阶上散落着褪色的蛇形徽章，每踩一步都会响起类似鳞片摩擦的沙沙声，仿佛有什么东西正在暗处吐着信子。"
                		+ " ";
                storyHistory.add(history);
                displayHistoryAndWait(history, 3);
            }
        });

        act1.addOption("找费尔奇", "需用「巧克力蛙」交换钥匙", new OptionAction() {
            @Override
            public void execute() {
            	hasGillyweed=true;
            	String history = "你觉得直接开锁风险太大，观察后窗又未必能有实质性的收获，于是决定去找费尔奇帮忙。你在城堡的走廊里四处寻找，终于在一间杂物间里找到了他。\r\n"
            			+ "\r\n"
            			+ "费尔奇正弯着腰整理着一堆旧扫帚，听到你的声音后，他直起身子，皱着眉头看着你。“你来干什么？小麻烦精。”他不耐烦地说道。你礼貌地向他说明了来意，希望他能给你猫头鹰棚屋的钥匙。\r\n"
            			+ "\r\n"
            			+ "费尔奇听后，冷笑一声：“钥匙？可不是随便就能给人的。除非……”他的目光落在了你手中的「巧克力蛙」上。这是你之前在霍格莫德村购买的，一直放在口袋里。你明白了他的意思，虽然有些不舍，但为了进入棚屋，你还是将「巧克力蛙」递给了他。\r\n"
            			+ "\r\n"
            			+ "费尔奇接过巧克力蛙，脸上露出一丝满意的笑容。他从腰间掏出一大串钥匙，在里面翻找了一会儿，然后将一把钥匙递给你：“拿好了，用完赶紧还回来。”你接过钥匙，顺利进入了棚屋。然而，因为没有选择其他方式进入，你错失了「灰烬线索」只在探索中获得了可能是斯内普遗留的「鳃囊草」 。\r\n"
            			+ "";
            	storyHistory.add(history);
                displayHistoryAndWait(history, 3);
            }
        });
        chapters.put(2, act1);

        // 第二幕：有求必应屋陷阱
        Chapter act2 = new Chapter("无论通过哪条路径抵达，有求必应屋的穹顶都在你踏入瞬间亮起猩红光芒。悬浮的水晶球里，日记本魂器的幻影正扭曲着渗出墨绿色黏液，而伏地魔的蛇瞳幻影从墙壁阴影中缓缓凝聚。他枯瘦的手指指向你腰间\r\n"
        		+ "\r\n"
        		+ "他那血红的眼睛紧紧盯着你，脸上挂着邪恶的笑容。“你以为你能阻止我？渺小的巫师。”他的声音在屋内回荡，充满了压迫感。你的任务是获取「蛇形匕首」，同时躲避伏地魔的幻影攻击。\r\n"
        		+ " ", "获取「蛇形匕首」，躲避伏地魔幻影。");
        act2.addOption("刺向幻影", "摧毁日记幻象，提前触发纳吉尼战斗", new OptionAction() {
            @Override
            public void execute() {
                if (magicPoints >= 20 && healthPoints >30) {
                    magicPoints -= 20;
                    hasSnakeDagger = true;
                    destroyedDiary = true;
                    foughtNagini = true;
                    healthPoints -= 30;
                    String history = "你心中燃起一股怒火，面对伏地魔的幻影，毫不畏惧。你举起手中的魔杖，口中念念有词，一道凌厉的魔法光芒射向幻影。幻影发出一声怒吼，试图躲避，但还是被光芒击中。趁着这个机会，你冲向水晶球，一把抓住了「蛇形匕首」。\r\n"
                    		+ "\r\n"
                    		+ "然而，你的攻击也激怒了幻影，它变得更加狂暴，不断向你发起攻击。你挥舞着魔杖和匕首，与幻影展开了激烈的战斗。在战斗过程中，你消耗了20点魔力，并且损失了30生命值，但成功摧毁了日记幻象。\r\n"
                    		+ "";
                    storyHistory.add(history);
                    displayHistoryAndWait(history, 4);
                } else {
                    showEnding("你心中燃起一股怒火，面对伏地魔的幻影，毫不畏惧。你举起手中的魔杖，口中念念有词，一道凌厉的魔法光芒射向幻影。幻影发出一声怒吼，试图躲避，但还是被光芒击中。趁着这个机会，你冲向水晶球，一把抓住了「蛇形匕首」。\r\n"
                    		+ "\r\n"
                    		+ "然而，你的攻击也激怒了幻影，它变得更加狂暴，不断向你发起攻击。你挥舞着魔杖和匕首，与幻影展开了激烈的战斗。\r\n"
                    		+ "而最终你魔力不足，无法进行有效攻击，被伏地魔幻影击中...", "BE：被幻影吞噬");
                }
                updateStatus();

            }
        });

        act2.addOption("隐藏匕首", "拖延时间，触发「无痕伸展咒」成就", new OptionAction() {
            private String history;

			@Override
            public void execute() {
                usedStealthSpell = true;
                hasSnakeDagger = true;
                String history = "你深知伏地魔幻影的强大，贸然攻击可能会陷入危险。于是，你决定先隐藏匕首，寻找其他机会。你悄悄地将「蛇形匕首」藏在衣服里，然后开始在屋内四处寻找可以躲避的地方。\r\n"
                		+ "\r\n"
                		+ "你发现屋子的一角有一个巨大的书架，书架后面有一个狭小的空间，刚好可以容纳一个人。你迅速躲了进去，屏住呼吸。伏地魔的幻影在屋内四处搜寻，它的脚步声在你耳边回荡，每一下都让你的心跳加速。\r\n"
                		+ "\r\n"
                		+ "不知过了多久，幻影似乎没有发现你的踪迹，渐渐消失了。你成功拖延了时间，并触发了「无痕伸展咒」成就 。同时，你保留了魔力，这为你后续的冒险提供了更多的可能性，也解锁了「策略大师」结局分支。\r\n"
                		+ "";
                storyHistory.add(history);
                achievements[0] = true; // 策略大师
                displayHistoryAndWait(history, 4);
                updateStatus();
            }
        });

        act2.addOption("逃跑", "遭遇食死徒，生命值-50", new OptionAction() {
            @Override
            public void execute() {
            	healthPoints -= 50;
                String history = "面对伏地魔的幻影，你心中充满了恐惧。你觉得自己根本无法与之抗衡，于是决定逃跑。你转身朝着门口的方向跑去，然而，在逃跑的过程中，你不小心触发了屋内的一个陷阱。\r\n"
                		+ "\r\n"
                		+ "地面突然裂开，你脚下一空，掉进了一个黑暗的深渊。在坠落的过程中，你拼命挥舞着双手，试图抓住什么东西。幸运的是，你在半空中抓住了一根突出的岩石，暂时稳住了身形。\r\n"
                		+ "\r\n"
                		+ "你抬头望去，发现食死徒们已经出现在了深渊上方，他们手中的魔杖闪烁着绿色的光芒，显然是准备对你发动攻击。你心中绝望，但还是鼓起勇气，试图爬上去。在与食死徒的战斗中，你的生命值减少了50点，如果失败，生命值将直接归零。成功逃脱后，你进入了「逃亡路线」，但这也导致你的结局受到了限制 。\r\n"
                		+ "";
                storyHistory.add(history);
                if (healthPoints > 0) {
                    displayHistoryAndWait(history, 4);
                } else {
                    showEnding("面对伏地魔的幻影，你心中充满了恐惧。你觉得自己根本无法与之抗衡，于是决定逃跑。你转身朝着门口的方向跑去，然而，在逃跑的过程中，你不小心触发了屋内的一个陷阱。\r\n"
                    		+ "\r\n"
                    		+ "地面突然裂开，你脚下一空，掉进了一个黑暗的深渊。在坠落的过程中，你拼命挥舞着双手，试图抓住什么东西。幸运的是，你在半空中抓住了一根突出的岩石，暂时稳住了身形。\r\n"
                    		+ "\r\n"
                    		+ "你抬头望去，发现食死徒们已经出现在了深渊上方，他们手中的魔杖闪烁着绿色的光芒，显然是准备对你发动攻击。你心中绝望，但还是鼓起勇气，试图爬上去。但最后被食死徒击中，生命值归零...", "BE：逃亡失败");
                }
                updateStatus();
            }

        });
        chapters.put(3, act2);

        // 第三幕：黑湖祭典
        Chapter act3 = new Chapter("离开有求必应屋，你根据之前获得的线索，来到了黑湖湖畔。夜晚的黑湖神秘而寂静，湖水在月光下闪烁着诡异的光芒。突然，一只巨大的巨乌贼从湖中探出了头，它的眼睛如灯笼般明亮，盯着你看了一会儿，然后发出了低沉的声音：“我闻到了鳃囊草的味道，如果你能给我并且通过我的拼写咒语挑战，我可以帮你去获得金杯。", "获取「赫奇帕奇金杯」\r\n"
        		+ "\r\n"
        		+ "咒语列表：\r\n"
        		+ "\r\n"
        		+ "            \"AquaEterna\",     // 永恒之水\r\n"
        		+ "            \"LumosMaxima\",    // 荧光闪烁\r\n"
        		+ "            \"Nox\",            // 诺克斯（熄灯咒）\r\n"
        		+ "            \"WingardiumLeviosa\", // 悬浮咒\r\n"
        		+ "            \"ExpectoPatronum\" // 呼神护卫");
        act3.addOption("给予鳃囊草并同意挑战", "开始咒语拼写挑战。", new OptionAction() {
            @Override
            public void execute() {
            	if (hasGillyweed) {
            		boolean gameResult = spellCastingGame();
                    if (gameResult) {
                        hasHufflepuffCup = true;
                        completedBlackLake = true;
                        String history = "你拿出在猫头鹰棚屋找到的「鳃囊草」 。你没有犹豫，将鳃囊草递给了巨乌贼。巨乌贼接过鳃囊草后，满意地晃了晃脑袋，然后潜入了湖中。\r\n"
                                + "\r\n"
                                + "不一会儿，它再次浮出水面，嘴里叼着一个闪闪发光的金杯——「赫奇帕奇金杯」。它将金杯放在你面前，说道：“这是我给你的报酬。”你接过金杯，心中充满喜悦。这个金杯不仅具有重要的历史价值，更解锁了「双魂器摧毁」路线，为你后续对抗伏地魔降低了BOSS战的难度 。\r\n"
                                + "";
                        storyHistory.add(history);
                        displayHistoryAndWait(history, 5);
                    } else {
                        showEnding("你尝试拼写咒语失败，巨乌贼感到不满，游走了，你没有获得「赫奇帕奇金杯」。", "BE：咒语拼写失败");
                    }
                }
                updateStatus();
            }
        });

        act3.addOption("拒绝给予鳃囊草", "被巨乌贼攻击，生命值归零", new OptionAction() {
            @Override
            public void execute() {
                showEnding("你看着巨乌贼，心中充满警惕，担心这是一个陷阱。你拒绝了它的要求，说道：“不，我不能把鳃囊草给你。”巨乌贼听后，眼中闪过一丝愤怒，它的触手在水中挥舞，激起巨大的水花。\r\n"
                		+ "\r\n"
                		+ "“那就别怪我不客气了。”巨乌贼说完，便向你发起了攻击。它的触手如鞭子般抽打过来，你试图躲避，但还是被击中了。在与巨乌贼的战斗中，你的生命值逐渐减少，最终归零。\r\n"
                		+ "", "BE：被巨乌贼攻击，葬身湖底");
            }
        });

        chapters.put(4, act3);

        // 第四幕：密室终局
        Chapter finalAct = new Chapter("经过一系列的冒险，你终于来到了密室核心。这里弥漫着一股腐臭的气息，四周的墙壁上爬满了诡异的藤蔓。纳吉尼守护在魂器旁边，它那巨大的身躯在昏暗的光线中若隐若现，眼睛闪烁着冰冷的光芒。与此同时，伏地魔的幻影再次降临，他的笑声在密室中回荡，令人胆寒。", "做出最终抉择。");
        finalAct.addOption("用匕首摧毁日记，用金杯困住纳吉尼", "双魂器摧毁，邓布利多救援", new OptionAction() {
            @Override
            public void execute() {
            	if (hasSnakeDagger && hasHufflepuffCup) {
                    String history1 = "你手中握着「蛇形匕首」，同时拥有「赫奇帕奇金杯」。面对纳吉尼和伏地魔的幻影，你心中有了计划。你先将目标对准了日记幻象，挥舞着匕首刺向它。随着一声惨叫，日记幻象被成功摧毁。\r\n"
                    		+ "\r\n"
                    		+ "紧接着，你利用「赫奇帕奇金杯」的力量，将纳吉尼困在了一个金色的光芒之中。纳吉尼在光芒中挣扎着，但始终无法逃脱。就在这时，邓布利多教授带着援军赶到，他们施展出强大的魔法，将伏地魔的幻影彻底驱散。你成功摧毁了双魂器，达成了HE：完美结局，并且解锁了时间转换器。这个时间转换器或许能在未来的冒险中发挥巨大的作用，让你有机会改变一些事情 。\r\n"
                    		+ "";
                    String history2 = "邓布利多及时赶到，成功救援了你";
                    storyHistory.add(history1);
                    storyHistory.add(history2);
                    achievements[1] = true; // 魂器猎人
                    displayHistoryAndWait(history1 + "\n\n" + history2, -1);
                    showEnding("你成功摧毁了多个魂器，帮助扭转了战局", "HE：完美结局（解锁时间转换器）");
                } else {
                    showEnding("你准备不足，无法完成双魂器摧毁...", "BE：准备不足");
                }
                updateStatus();

            }
        });

        finalAct.addOption("用匕首攻击纳吉尼", "单魂器摧毁，触发BOSS战", new OptionAction() {
            @Override
            public void execute() {
            	if (hasSnakeDagger && magicPoints >= 50) {
                    magicPoints -= 50;
                    String history1 = "没有「赫奇帕奇金杯」，你只能凭借手中的「蛇形匕首」与纳吉尼和伏地魔的幻影战斗。你深吸一口气，集中精神，向纳吉尼发起了攻击。在战斗过程中，你需要消耗至少50点魔力来施展强大的魔法。\r\n"
                    		+ "\r\n"
                    		+ "这场战斗异常激烈，你与纳吉尼你来我往，互不相让。你的身上多处受伤，但你始终没有放弃。最终，你成功摧毁了单魂器，触发了BOSS战。"
                    		+ "\r\n"
                    		+ "生命值减少50";
                    String history2 = "经过一场艰苦的战斗，你战胜了纳吉尼";
                    storyHistory.add(history1);
                    storyHistory.add(history2);
                    displayHistoryAndWait(history1 + "\n\n" + history2, -1);
                    showEnding("你成功摧毁了一个魂器，返回了现实世界", "HE：普通结局（返回现实）");
                } else {
                    showEnding("你无法有效攻击纳吉尼，被它击败...", "BE：被纳吉尼击败");
                }
                updateStatus();

            }
        });

        finalAct.addOption("尝试与伏地魔谈判", "危险的尝试", new OptionAction() {
            @Override
            public void execute() {
            	if (bribedSnape) {
                    String history = "腐臭的气息如实质般凝滞在密室内，墙壁上盘曲的蛇形藤蔓渗出墨绿色黏液，在石砖上汇成蜿蜒的血线。纳吉尼盘踞在魂器基座旁，三角形的头颅微微扬起，分叉的信子吞吐间散发出浓烈的腥气，鳞片在魔杖光芒下泛着冰冷的金属光泽。伏地魔的幻影从穹顶阴影中缓缓降下，骨节分明的手指抚过石墙上的蛇形浮雕，猩红瞳孔锁定你时，周遭的空气骤然冻结 —— 你空无一物的掌心甚至能感受到那股源自灵魂的寒意。"
                    		+"\r\n"
                    		+ "";
                    storyHistory.add(history);
                    displayHistoryAndWait(history, -1);
                    showEnding("腐臭的藤蔓在石壁上渗出脓液，纳吉尼的鳞片擦过石砖的声响如同死神的磨刀声。你跌坐在魂器基座前，魔杖脱手滚落在伏地魔幻影的脚边 —— 那团由墨绿色黏液凝聚的身影正缓缓抬起手，指尖即将迸发出索命的绿光。就在这千钧一发之际，右侧石柱的阴影中突然传来布料摩擦的轻响，斯内普教授裹着黑袍的身影从蛛网覆盖的角落走出，他的魔杖垂在身侧，瞳孔在昏暗光线下闪烁着难以捉摸的幽光。\r\n"
                    		+ "\r\n"
                    		+ "「教授！救我！」你挣扎着向他伸出手，袖口因纳吉尼的缠绕而撕裂，「猫头鹰棚屋的银币…… 我给过你银币的！」\r\n"
                    		+ "\r\n"
                    		+ "斯内普的嘴角勾起一抹冰冷的弧度，那笑容比摄魂怪的气息更令人战栗。他踢开脚边一块蛇形浮雕碎片，靴底碾过石砖的声响在死寂中格外刺耳：「那枚银币？」他顿了顿，从袍子里掏出一枚泛着微光的硬币，指尖捏着币面轻轻晃动，「连买通费尔奇的巧克力蛙都不够，你以为能收买一个食死徒？」\r\n"
                    		+ "\r\n"
                    		+ "硬币从他指间坠落，在石砖上弹起又落下，滚到你沾满血污的手掌旁。伏地魔的幻影发出低沉的笑声，猩红瞳孔在斯内普身上停顿片刻，仿佛默许了这场背叛的闹剧。「当时不过是看你像条摇尾乞怜的小狗，」斯内普的声音陡然变冷，魔杖尖端对准你的胸口，「真以为西弗勒斯・斯内普会为了几个银西可背叛黑魔王？」\r\n"
                    		+ "\r\n"
                    		+ "纳吉尼的嘶吼骤然响起，三角头颅如离弦之箭般冲向你的咽喉。你下意识地看向斯内普，却只见他侧身避开，黑袍下摆扫过你颤抖的肩膀。毒液刺入皮肉的瞬间，你听见斯内普在阴影中低语，那声音混着伏地魔的狂笑和藤蔓摇曳的声响，化作最后一道催命符：「蠢货…… 你的血，不过是纳吉尼的开胃菜。」"
                    		+ "\r\n"
                    		+ "\r\n"
                    		+ "墨绿色的毒液在血管里炸开，视线模糊中，你看见斯内普弯腰拾起那枚被你遗忘的银币，在伏地魔的幻影前微微颔首。纳吉尼的身体如巨蟒绞索般收紧，将你最后一声呜咽碾碎在密室的腐臭里 —— 原来那枚救命的银币，从一开始就是刻着死亡印记的诱饵。", "BE：背叛结局");
                } else {
                    showEnding("你密室内的蛇形藤蔓突然剧烈抽搐，墨绿色黏液如瀑布般从穹顶滴落，在石砖上砸出冒烟的蚀痕。纳吉尼抬起三角头颅，红宝石般的瞳孔锁定你颤抖的喉结，分叉的信子吞吐间，空气中弥漫开浓得化不开的腥甜 —— 那是死亡的气息。伏地魔的幻影悬浮在魂器上方，骨节分明的手指正穿透日记本魂器，渗出的黏液在他指尖凝聚成毒蛇的轮廓。"
                    		+ "「连魔杖都握不稳的废物」腐臭的气息喷在你脸上，那是无数灵魂被吞噬后的残响。「在我眼里，你和脚下的黏液没有区别。」\r\n"
                    		+ "\r\n"
                    		+ "你挣扎着去够腰间的假匕首，却只摸到一片冰冷的鳞片 —— 纳吉尼不知何时已盘绕在你身后，冰凉的躯体如铁链般收紧。伏地魔的幻影松开手，你跌落在地时，听见他用蛇佬腔吐出一个词：「嘶…… 纳吉尼。」", "BE：谈判失败");
                }
                updateStatus();
            }

        });
        chapters.put(5, finalAct);
    }
    
    private void displayHistoryAndWait(String history, int nextChapter) {
        // 显示历史记录并等待用户点击
        storyArea.setText(history);
        storyArea.setForeground(new Color(50, 30, 0));
        
        // 隐藏选项面板
        optionsPanel.removeAll();
        optionsPanel.revalidate();
        optionsPanel.repaint();
        
        // 添加提示文本
        storyArea.append("\n\n点击屏幕继续...");
        
        // 设置状态，准备进入下一章节
        currentChapter = nextChapter;
        isReadyForNextChapter = true;
    }
    
    

    private void showChapter(int chapterId) {
    	if (chapterId == -1) {
            return;
        }

        currentChapter = chapterId;
        Chapter chapter = chapters.get(chapterId);

        // 更新任务标签
        switch (chapterId) {
        	case 0:
        		currentTaskLabel.setText("当前任务: 零幕 - 副本开端");
        		break;
            case 1:
                currentTaskLabel.setText("当前任务: 序章 - 蓝光裂隙");
                break;
            case 2:
                currentTaskLabel.setText("当前任务: 第一幕 - 礼堂疑云");
                break;
            case 3:
                currentTaskLabel.setText("当前任务: 第二幕 - 有求必应屋陷阱");
                break;
            case 4:
                currentTaskLabel.setText("当前任务: 第三幕 - 黑湖祭典支线");
                break;
            case 5:
                currentTaskLabel.setText("当前任务: 第四幕 - 密室终局");
                break;
        }

        // 显示剧情
        currentChapterText = chapter.getStoryText() + "\n\n任务: " + chapter.getTask();
        storyArea.setText(currentChapterText);
        storyArea.setForeground(new Color(50, 30, 0));


        
        // 显示选项
        optionsPanel.removeAll();
        for (Option option : chapter.getOptions()) {
            JButton optionButton = createStyledButton(option.getOptionText());
            optionButton.setPreferredSize(new Dimension(220, 50));
            optionButton.setMaximumSize(new Dimension(220, 50));
            optionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    option.getAction().execute();
                }
            });

            // 添加选项描述
            JLabel descriptionLabel = new JLabel(option.getDescription());
            descriptionLabel.setForeground(new Color(200, 180, 140));
            descriptionLabel.setBorder(new EmptyBorder(0, 10, 10, 0));

            JPanel optionContainer = new JPanel(new BorderLayout());
            optionContainer.setBackground(backgroundColor);
            optionContainer.setBorder(new EmptyBorder(5, 0, 5, 0));
            optionContainer.add(optionButton, BorderLayout.NORTH);
            optionContainer.add(descriptionLabel, BorderLayout.CENTER);

            optionsPanel.add(optionContainer);
        }

        // 添加一些空白空间
        optionsPanel.add(Box.createVerticalGlue());

        optionsPanel.revalidate();
        optionsPanel.repaint();

        // 添加平滑滚动到顶部
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JScrollBar vertical = storyScrollPane.getVerticalScrollBar();
                vertical.setValue(0);
            }
        });
    }

    private void showEnding(String story, String endingName) {
    	storyArea.setText(story + "\n\n" + "结局: " + endingName + "\n\n");
        storyArea.setForeground(new Color(50, 30, 0));

        // 显示成就
        if (endingName.startsWith("HE")) {
            storyArea.append("获得成就: \n");
            if (achievements[0]) storyArea.append("- 策略大师: 全程未触发战斗\n");
            if (achievements[1]) storyArea.append("- 魂器猎人: 摧毁所有已知魂器\n");
            if (achievements[2]) storyArea.append("- 历史改写者: 达成HE+结局\n");
        }

        // 隐藏选项，显示重新开始按钮
        optionsPanel.removeAll();
        JButton restartButton = createStyledButton("重新开始");
        restartButton.setPreferredSize(new Dimension(220, 50));
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
                showChapter(0);
            }
        });

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBackground(backgroundColor);
        buttonContainer.setBorder(new EmptyBorder(20, 0, 20, 0));
        buttonContainer.add(restartButton, BorderLayout.CENTER);

        optionsPanel.add(buttonContainer);
        optionsPanel.add(Box.createVerticalGlue());

        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void updateStatus() {
        magicPointsLabel.setText("魔力值: " + magicPoints);
        healthPointsLabel.setText("生命值: " + healthPoints);
        currentTaskLabel.setText("当前任务: 序章 - 蓝光裂隙");

        // 检查游戏是否结束
        if (healthPoints <= 0) {
            showEnding("你的生命值归零，游戏结束...", "BE：生命耗尽");
        }
    }

    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存游戏");
        int returnValue = fileChooser.showSaveDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
                // 保存游戏状态
                GameState state = new GameState(
                        magicPoints, timeLeft, healthPoints,
                        hasWand, hasMagicCoin, hasGillyweed, hasSnakeDagger, hasHufflepuffCup,
                        hasAshesFragment, bribedSnape, completedBlackLake, foughtNagini,
                        destroyedDiary, usedStealthSpell, achievements, currentChapter, storyHistory
                );
                oos.writeObject(state);

                // 显示保存成功的动画效果
                JOptionPane.showMessageDialog(this, "游戏保存成功！", "保存游戏", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "保存游戏失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("加载游戏");
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile))) {
                // 加载游戏状态
                GameState state = (GameState) ois.readObject();
                magicPoints = state.magicPoints;
                timeLeft = state.timeLeft;
                healthPoints = state.healthPoints;
                hasWand = state.hasWand;
                hasMagicCoin = state.hasMagicCoin;
                hasGillyweed = state.hasGillyweed;
                hasSnakeDagger = state.hasSnakeDagger;
                hasHufflepuffCup = state.hasHufflepuffCup;
                hasAshesFragment = state.hasAshesFragment;
                bribedSnape = state.bribedSnape;
                completedBlackLake = state.completedBlackLake;
                foughtNagini = state.foughtNagini;
                destroyedDiary = state.destroyedDiary;
                usedStealthSpell = state.usedStealthSpell;
                achievements = state.achievements;
                currentChapter = state.currentChapter;
                storyHistory = state.storyHistory;

                updateStatus();
                showChapter(currentChapter);

                // 显示加载成功的动画效果
                JOptionPane.showMessageDialog(this, "游戏加载成功！", "加载游戏", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "加载游戏失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetGame() {
        magicPoints = 50;
        timeLeft = 72;
        healthPoints = 100;
        hasWand = false;
        hasMagicCoin = false;
        hasGillyweed = false;
        hasSnakeDagger = false;
        hasHufflepuffCup = false;
        hasAshesFragment = false;
        bribedSnape = false;
        completedBlackLake = false;
        foughtNagini = false;
        destroyedDiary = false;
        usedStealthSpell = false;
        Arrays.fill(achievements, false);
        currentChapter = 0;
        storyHistory.clear();
        isReadyForNextChapter = false;

        updateStatus();
    }

    // 内部类：剧情章节
    private class Chapter {
        private String storyText;
        private String task;
        private List<Option> options = new ArrayList<>();

        public Chapter(String storyText, String task) {
            this.storyText = storyText;
            this.task = task;
        }

        public void addOption(String optionText, String description, OptionAction action) {
            options.add(new Option(optionText, description, action));
        }

        public String getStoryText() {
            return storyText;
        }

        public String getTask() {
            return task;
        }

        public List<Option> getOptions() {
            return options;
        }
    }

    // 内部类：选项
    private class Option {
        private String optionText;
        private String description;
        private OptionAction action;

        public Option(String optionText, String description, OptionAction action) {
            this.optionText = optionText;
            this.description = description;
            this.action = action;
        }

        public String getOptionText() {
            return optionText;
        }

        public String getDescription() {
            return description;
        }

        public OptionAction getAction() {
            return action;
        }
    }

    // 内部接口：选项动作
    private interface OptionAction {
        void execute();
    }

    // 内部类：游戏状态（用于存档）
    private static class GameState implements Serializable {
        int magicPoints;
        int timeLeft;
        int healthPoints;
        boolean hasWand;
        boolean hasMagicCoin;
        boolean hasGillyweed;
        boolean hasSnakeDagger;
        boolean hasHufflepuffCup;
        boolean hasAshesFragment;
        boolean bribedSnape;
        boolean completedBlackLake;
        boolean foughtNagini;
        boolean destroyedDiary;
        boolean usedStealthSpell;
        boolean[] achievements;
        int currentChapter;
        List<String> storyHistory;

        public GameState(int magicPoints, int timeLeft, int healthPoints,
                         boolean hasWand, boolean hasMagicCoin, boolean hasGillyweed,
                         boolean hasSnakeDagger, boolean hasHufflepuffCup, boolean hasAshesFragment,
                         boolean bribedSnape, boolean completedBlackLake, boolean foughtNagini,
                         boolean destroyedDiary, boolean usedStealthSpell, boolean[] achievements,
                         int currentChapter, List<String> history) {
            this.magicPoints = magicPoints;
            this.timeLeft = timeLeft;
            this.healthPoints = healthPoints;
            this.hasWand = hasWand;
            this.hasMagicCoin = hasMagicCoin;
            this.hasGillyweed = hasGillyweed;
            this.hasSnakeDagger = hasSnakeDagger;
            this.hasHufflepuffCup = hasHufflepuffCup;
            this.hasAshesFragment = hasAshesFragment;
            this.bribedSnape = bribedSnape;
            this.completedBlackLake = completedBlackLake;
            this.foughtNagini = foughtNagini;
            this.destroyedDiary = destroyedDiary;
            this.usedStealthSpell = usedStealthSpell;
            this.achievements = achievements;
            this.currentChapter = currentChapter;
            this.storyHistory = storyHistory;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HogwartsAdventure().setVisible(true);
            }
        });
    }
}