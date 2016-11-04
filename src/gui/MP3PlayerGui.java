/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import com.jtattoo.plaf.aero.AeroLookAndFeel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import objects.MP3;
import objects.MP3Player;
import utils.FileUtils;
import utils.MP3PlayerFileFilter;
import utils.SkinUtils;

/**
 *
 * @author Влад
 */
public class MP3PlayerGui extends javax.swing.JFrame implements BasicPlayerListener{
  
    private static final String MP3_FILE_EXTENSION = "mp3";
    private static final String MP3_FILE_DESCRIPTION = "Файлы mp3";
    private static final String PLAYLIST_FILE_EXTENSION = "pls";
    private static final String PLAYLIST_FILE_DESCRIPTION = "Файлы плейлиста";
    private static final String EMPTY_STRING = "";
    private static final String INPUT_SONG_NAME = "введите имя пестни";
    private static final String EMPTY_AUTHOR_NAME = "Unknow Author";
    
    private DefaultListModel mp3ListModel = new DefaultListModel();
    private FileFilter mp3FileFilter = new MP3PlayerFileFilter(MP3_FILE_EXTENSION, MP3_FILE_DESCRIPTION);
    private FileFilter playlistFileFilter = new MP3PlayerFileFilter(PLAYLIST_FILE_EXTENSION, PLAYLIST_FILE_DESCRIPTION);
    private MP3Player player = new MP3Player(this);
    
    private int currentVolumeValue; // for toggle button
    
    private long secondsAmount;
    private long duration;
    private int bytesLen;
    private double posValue = 0.0;
    private boolean movingFromJump = false;
    private boolean moveAutomatic = false;
        
//<editor-fold defaultstate="collapsed" desc="listeners of Basic player">
    @Override
    public void opened(Object o, Map map) {
        duration = (long) Math.round((((Long) map.get("duration")).longValue()) / 1000000);
        bytesLen = (int) Math.round(((Integer) map.get("mp3.length.bytes")).intValue());
        System.out.println(map);
        String songName = map.get("title") !=null ? map.get("title").toString() : FileUtils.getFileNameWithoutExtension(new File(o.toString()).getName());
        String songAuthor = map.get("author") !=null ? map.get("author").toString() : EMPTY_AUTHOR_NAME;
        
        if (songName.length() > 30 ) {
            songName = songName.substring(0, 30) + "...";            
        }

        txtCurrentPlay.setText(songAuthor + " - " + songName);
        
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        txtGeneralPlayTime.setText(sdf.format(new Date(duration * 1000)));
    }
    
    @Override
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
        float progress = -1.0f;
        
        if ((bytesread > 0) && (duration > 0)) {
            progress = bytesread * 1.0f / bytesLen * 1.0f;
        }
        
        secondsAmount = (long) (duration * progress);
        if (duration != 0 && movingFromJump == false) {
            slideProgress.setValue(((int) Math.round(secondsAmount * 1000 / duration)));
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        txtCurrentPlayTime.setText(sdf.format(new Date(secondsAmount * 1000)));
    }
    
    @Override
    public void stateUpdated(BasicPlayerEvent bpe) {
        int state = bpe.getCode();
        
        if (state == BasicPlayerEvent.PLAYING) {
            movingFromJump = false;                     
        } else if (state == BasicPlayerEvent.SEEKING) {
            movingFromJump = true;                      //manual seeking
        } else if (state == BasicPlayerEvent.EOM) {
            if (selectNextSong()) {
                playFile();
            }
        }
    }
    
    @Override
    public void setController(BasicController bc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
//</editor-fold>

    /**
     * Creates new form MP3PlayerGui
     */
    public MP3PlayerGui() {
        initComponents();
    }
    
    
    private void playFile() {
        int[] indexPlayList = lstPlayList.getSelectedIndices();
        if (indexPlayList.length > 0) {
            MP3 mp3 = (MP3) mp3ListModel.getElementAt(indexPlayList[0]);
            player.play(mp3.getPath());
            player.setVolume(sliderVolume.getValue(), sliderVolume.getMaximum());
            System.out.println(mp3.getPath());
            //txtCurrentPlay.setText(mp3.getName());
        }
    }
    
    private boolean selectPrevSong() {
        int nextIndex = lstPlayList.getSelectedIndex() - 1;
        if (nextIndex >= 0) {
            lstPlayList.setSelectedIndex(nextIndex);
            return true;
        }
        return false;
    }
    
    private boolean selectNextSong() {
        int nextIndex = lstPlayList.getSelectedIndex() + 1;
        if (nextIndex <= lstPlayList.getModel().getSize() - 1) {
            lstPlayList.setSelectedIndex(nextIndex);
            return true;
        }
        return false;
    }

    private void searchSong() {
        String searchStr = txtSearch.getText();
        if (searchStr == null || searchStr.trim().equals(EMPTY_STRING)) {
            return;
        }

        ArrayList<Integer> mp3findedIndexes = new ArrayList<Integer>();
        for (int i = 0; i < mp3ListModel.size(); i++) {
            MP3 mp3 = (MP3) mp3ListModel.getElementAt(i);
            if (mp3.getName().toUpperCase().contains(searchStr.toUpperCase())) {
                mp3findedIndexes.add(i);
            }
        }

        int[] selectedIndexes = new int[mp3findedIndexes.size()];

        if (selectedIndexes.length == 0) {
            JOptionPane.showMessageDialog(this, "Поиск по строке \'" + searchStr + "\' не дал результатов", "Поиск", JOptionPane.INFORMATION_MESSAGE,
                    new javax.swing.ImageIcon(getClass().getResource("/images/Start-Menu-Search-icon.png")));
            txtSearch.requestFocus();
            txtSearch.selectAll();
            return;
        }

        for (int i = 0; i < selectedIndexes.length; i++) {
            selectedIndexes[i] = mp3findedIndexes.get(i).intValue();
        }

        lstPlayList.setSelectedIndices(selectedIndexes);

    }
    
    private void processSeek(double bytes) {
        try {
            long skipBytes = (long) Math.round(((Integer) bytesLen).intValue() * bytes);
            player.jump(skipBytes);
        } catch (Exception e) {
            e.printStackTrace();
            movingFromJump = false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        popMenu = new javax.swing.JPopupMenu();
        popPlaySong = new javax.swing.JMenuItem();
        popPauseSong = new javax.swing.JMenuItem();
        popStopSong = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        popAddSong = new javax.swing.JMenuItem();
        popDelSong = new javax.swing.JMenuItem();
        popOpenPlaylist = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        popClearPlaylist = new javax.swing.JMenuItem();
        panelSearch = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        panelMain = new javax.swing.JPanel();
        btnPlaySong = new javax.swing.JButton();
        btnPrevSong = new javax.swing.JButton();
        btnPauseSong = new javax.swing.JButton();
        btnNextSong = new javax.swing.JButton();
        btnStopSong = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstPlayList = new javax.swing.JList();
        sliderVolume = new javax.swing.JSlider();
        tglbtnVolume = new javax.swing.JToggleButton();
        btnAddSong = new javax.swing.JButton();
        btnDeleteSong = new javax.swing.JButton();
        btnSelectPrev = new javax.swing.JButton();
        btnSelectNext = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        txtCurrentPlay = new javax.swing.JLabel();
        slideProgress = new javax.swing.JSlider();
        txtGeneralPlayTime = new javax.swing.JLabel();
        txtCurrentPlayTime = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuOpenPlaylist = new javax.swing.JMenuItem();
        menuSavePlaylist = new javax.swing.JMenuItem();
        menuSeparator = new javax.swing.JPopupMenu.Separator();
        menuExit = new javax.swing.JMenuItem();
        menuPrefs = new javax.swing.JMenu();
        menuChangeSkin = new javax.swing.JMenu();
        menuSkin1 = new javax.swing.JMenuItem();
        menuSkin2 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogTitle("Выбрать файл");
        fileChooser.setMultiSelectionEnabled(true);

        popMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        popMenu.setMinimumSize(new java.awt.Dimension(50, 50));

        popPlaySong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Play.png"))); // NOI18N
        popPlaySong.setText("Play");
        popPlaySong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popPlaySongActionPerformed(evt);
            }
        });
        popMenu.add(popPlaySong);

        popPauseSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Pause-icon.png"))); // NOI18N
        popPauseSong.setText("Pause");
        popPauseSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popPauseSongActionPerformed(evt);
            }
        });
        popMenu.add(popPauseSong);

        popStopSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop-red-icon.png"))); // NOI18N
        popStopSong.setText("Stop");
        popStopSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popStopSongActionPerformed(evt);
            }
        });
        popMenu.add(popStopSong);
        popMenu.add(jSeparator4);

        popAddSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus_16.png"))); // NOI18N
        popAddSong.setText("Добавить пестню");
        popAddSong.setToolTipText("добавление пестни");
        popAddSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popAddSongActionPerformed(evt);
            }
        });
        popMenu.add(popAddSong);

        popDelSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/remove_icon.png"))); // NOI18N
        popDelSong.setText("Удалить пестню");
        popDelSong.setToolTipText("убдаление пестни");
        popDelSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popDelSongActionPerformed(evt);
            }
        });
        popMenu.add(popDelSong);

        popOpenPlaylist.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-icon.png"))); // NOI18N
        popOpenPlaylist.setText("Открыть плейлист");
        popOpenPlaylist.setToolTipText("открытие плейлиста");
        popOpenPlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popOpenPlaylistActionPerformed(evt);
            }
        });
        popMenu.add(popOpenPlaylist);
        popMenu.add(jSeparator3);

        popClearPlaylist.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Actions-edit-clear-icon.png"))); // NOI18N
        popClearPlaylist.setText("Очистить плейлист");
        popClearPlaylist.setToolTipText("Очистить плейлист польностью");
        popClearPlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popClearPlaylistActionPerformed(evt);
            }
        });
        popMenu.add(popClearPlaylist);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mp3 player one");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        panelSearch.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        txtSearch.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        txtSearch.setForeground(new java.awt.Color(153, 153, 153));
        txtSearch.setText(INPUT_SONG_NAME);
        txtSearch.setToolTipText("для ввода текста");
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtSearchFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSearchFocusLost(evt);
            }
        });
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSearchKeyPressed(evt);
            }
        });

        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/search_16.png"))); // NOI18N
        btnSearch.setText("Найти");
        btnSearch.setToolTipText("Найти пестню");
        btnSearch.setActionCommand("Найтии");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSearchLayout = new javax.swing.GroupLayout(panelSearch);
        panelSearch.setLayout(panelSearchLayout);
        panelSearchLayout.setHorizontalGroup(
            panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(17, 17, 17))
        );
        panelSearchLayout.setVerticalGroup(
            panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelMain.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnPlaySong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Play.png"))); // NOI18N
        btnPlaySong.setToolTipText("играть");
        btnPlaySong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlaySongActionPerformed(evt);
            }
        });

        btnPrevSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/prev-icon.png"))); // NOI18N
        btnPrevSong.setToolTipText("предыдущая пестня");
        btnPrevSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevSongActionPerformed(evt);
            }
        });

        btnPauseSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Pause-icon.png"))); // NOI18N
        btnPauseSong.setToolTipText("поставить на паузу");
        btnPauseSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseSongActionPerformed(evt);
            }
        });

        btnNextSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/next-icon.png"))); // NOI18N
        btnNextSong.setToolTipText("следущая пестня");
        btnNextSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextSongActionPerformed(evt);
            }
        });

        btnStopSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop-red-icon.png"))); // NOI18N
        btnStopSong.setToolTipText("остановить проигрывание");
        btnStopSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopSongActionPerformed(evt);
            }
        });

        lstPlayList.setModel(mp3ListModel);
        lstPlayList.setToolTipText("Список песен");
        lstPlayList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstPlayListMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                lstPlayListMouseReleased(evt);
            }
        });
        lstPlayList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lstPlayListKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(lstPlayList);

        sliderVolume.setMaximum(200);
        sliderVolume.setMinorTickSpacing(5);
        sliderVolume.setSnapToTicks(true);
        sliderVolume.setToolTipText("изменение громкости");
        sliderVolume.setValue(20);
        sliderVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderVolumeStateChanged(evt);
            }
        });

        tglbtnVolume.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/speaker.png"))); // NOI18N
        tglbtnVolume.setToolTipText("выключить звук");
        tglbtnVolume.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/mute.png"))); // NOI18N
        tglbtnVolume.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/mute.png"))); // NOI18N
        tglbtnVolume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglbtnVolumeActionPerformed(evt);
            }
        });

        btnAddSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus_16.png"))); // NOI18N
        btnAddSong.setToolTipText("добавление песни");
        btnAddSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSongActionPerformed(evt);
            }
        });

        btnDeleteSong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/remove_icon.png"))); // NOI18N
        btnDeleteSong.setToolTipText("удаление песни");
        btnDeleteSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteSongActionPerformed(evt);
            }
        });

        btnSelectPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow-up-icon.png"))); // NOI18N
        btnSelectPrev.setToolTipText("выделить след песню");
        btnSelectPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectPrevActionPerformed(evt);
            }
        });

        btnSelectNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow-down-icon.png"))); // NOI18N
        btnSelectNext.setToolTipText("выделить предидущую песню");
        btnSelectNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectNextActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        txtCurrentPlay.setText("no song");

        slideProgress.setMaximum(1000);
        slideProgress.setMinorTickSpacing(1);
        slideProgress.setSnapToTicks(true);
        slideProgress.setToolTipText("Позиция");
        slideProgress.setValue(0);
        slideProgress.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slideProgressStateChanged(evt);
            }
        });

        txtGeneralPlayTime.setText("-----");
        txtGeneralPlayTime.setToolTipText("общее время");

        txtCurrentPlayTime.setText("----");
        txtCurrentPlayTime.setToolTipText("текущее время");

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnPrevSong, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnPlaySong, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(btnPauseSong, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnStopSong, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnNextSong, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                        .addComponent(tglbtnVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sliderVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(panelMainLayout.createSequentialGroup()
                                    .addComponent(btnAddSong, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnDeleteSong, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(2, 2, 2)
                                    .addComponent(btnSelectPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnSelectNext, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jScrollPane1)
                                .addComponent(slideProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addComponent(txtCurrentPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtGeneralPlayTime, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addComponent(txtCurrentPlayTime)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAddSong, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDeleteSong, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnSelectPrev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSelectNext, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCurrentPlay, javax.swing.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
                    .addComponent(txtGeneralPlayTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCurrentPlayTime)
                .addGap(5, 5, 5)
                .addComponent(slideProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tglbtnVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sliderVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnPrevSong, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStopSong, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPauseSong, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPlaySong, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNextSong, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        menuFile.setText("Файл");
        menuFile.setToolTipText("работа с файлом");

        menuOpenPlaylist.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-icon.png"))); // NOI18N
        menuOpenPlaylist.setText("Открыть плейлист");
        menuOpenPlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenPlaylistActionPerformed(evt);
            }
        });
        menuFile.add(menuOpenPlaylist);

        menuSavePlaylist.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_16.png"))); // NOI18N
        menuSavePlaylist.setText("Сохранить плейлист");
        menuSavePlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSavePlaylistActionPerformed(evt);
            }
        });
        menuFile.add(menuSavePlaylist);
        menuFile.add(menuSeparator);

        menuExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/exit.png"))); // NOI18N
        menuExit.setText("Выход");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        menuFile.add(menuExit);

        jMenuBar1.add(menuFile);

        menuPrefs.setText("Сервис");
        menuPrefs.setToolTipText("");

        menuChangeSkin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/gear_16.png"))); // NOI18N
        menuChangeSkin.setText("Внешний вид");

        menuSkin1.setText("Скин 1");
        menuSkin1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeSkin1(evt);
            }
        });
        menuChangeSkin.add(menuSkin1);

        menuSkin2.setText("Скин 2");
        menuSkin2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeSkin2(evt);
            }
        });
        menuChangeSkin.add(menuSkin2);
        menuChangeSkin.add(jSeparator2);

        jMenuItem1.setText("System Skin");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeSkinStandart(evt);
            }
        });
        menuChangeSkin.add(jMenuItem1);

        menuPrefs.add(menuChangeSkin);

        jMenuBar1.add(menuPrefs);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchSong();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void changeSkin1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeSkin1
        SkinUtils.changeSkin(this, new NimbusLookAndFeel(),fileChooser,popMenu);
    }//GEN-LAST:event_changeSkin1

    private void changeSkin2(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeSkin2
        SkinUtils.changeSkin(this, new AeroLookAndFeel(),fileChooser,popMenu);        
    }//GEN-LAST:event_changeSkin2

    private void changeSkinStandart(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeSkinStandart
        SkinUtils.changeSkin(this, UIManager.getSystemLookAndFeelClassName(),fileChooser,popMenu);
    }//GEN-LAST:event_changeSkinStandart

    private void btnAddSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSongActionPerformed
        FileUtils.addFileFilter(fileChooser, mp3FileFilter);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();

            for (File file : selectedFiles) {
                MP3 mp3 = new MP3(file.getName(), file.getPath());

                if (!mp3ListModel.contains(mp3)) {
                    mp3ListModel.addElement(mp3);
                } else {
                    System.out.println("file has already in the playlist");
                }                
            }
        }
    }//GEN-LAST:event_btnAddSongActionPerformed

    private void btnDeleteSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSongActionPerformed
        int[] indexPlayList = lstPlayList.getSelectedIndices();
        if (indexPlayList.length > 0) {
            ArrayList<MP3> mp3ListForRemove = new ArrayList<MP3>();
            for (int i = 0; i < indexPlayList.length; i++) {
                MP3 mp3 = (MP3) mp3ListModel.getElementAt(indexPlayList[i]);
                mp3ListForRemove.add(mp3);                
            }
            for (MP3 mp3 : mp3ListForRemove) {
                mp3ListModel.removeElement(mp3);
            }
        }        
    }//GEN-LAST:event_btnDeleteSongActionPerformed

    private void btnSelectNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectNextActionPerformed
        selectNextSong();
    }//GEN-LAST:event_btnSelectNextActionPerformed

    private void btnSelectPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectPrevActionPerformed
        selectPrevSong();
    }//GEN-LAST:event_btnSelectPrevActionPerformed

    private void btnPlaySongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaySongActionPerformed
        playFile();
    }//GEN-LAST:event_btnPlaySongActionPerformed

    private void menuSavePlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSavePlaylistActionPerformed
        FileUtils.addFileFilter(fileChooser, playlistFileFilter);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.exists()) {
                
                int resultOveride = JOptionPane.showConfirmDialog(this, "Файл существует", "Перезаписать?", JOptionPane.YES_NO_CANCEL_OPTION);
                switch(resultOveride) {
                    case JOptionPane.NO_OPTION:
                        menuSavePlaylistActionPerformed(evt);
                        return;
                    case JOptionPane.CANCEL_OPTION:
                        fileChooser.cancelSelection();
                        return;                            
                }
                fileChooser.approveSelection();
            }
            
            String fileExtension = FileUtils.getFileExtension(selectedFile);
            String fileNameForSave = (fileExtension !=null && fileExtension.equals(PLAYLIST_FILE_EXTENSION) ?
                    selectedFile.getPath() : selectedFile.getPath()+"."+PLAYLIST_FILE_EXTENSION);
            
            FileUtils.serialize(mp3ListModel, fileNameForSave);
        }
    }//GEN-LAST:event_menuSavePlaylistActionPerformed

    private void menuOpenPlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenPlaylistActionPerformed
        FileUtils.addFileFilter(fileChooser, playlistFileFilter);
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            DefaultListModel mp3ListModel = (DefaultListModel) FileUtils.deserialize(selectedFile.getPath());
            this.mp3ListModel = mp3ListModel;
            lstPlayList.setModel(mp3ListModel);
        }
    }//GEN-LAST:event_menuOpenPlaylistActionPerformed

    private void txtSearchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSearchFocusGained
        if (txtSearch.getText().equals(INPUT_SONG_NAME)) {
            txtSearch.setText(EMPTY_STRING);
            txtSearch.setForeground(Color.BLACK);
            txtSearch.setFont(new Font("Tahoma", Font.PLAIN, 11));
        }
    }//GEN-LAST:event_txtSearchFocusGained

    private void txtSearchFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSearchFocusLost
        if (txtSearch.getText().trim().equals(EMPTY_STRING)) {
            txtSearch.setText(INPUT_SONG_NAME);
            txtSearch.setForeground(Color.lightGray);
            txtSearch.setFont(new Font("Tahoma", Font.ITALIC, 11));
        }
    }//GEN-LAST:event_txtSearchFocusLost

    private void popAddSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popAddSongActionPerformed
        btnAddSongActionPerformed(evt);
    }//GEN-LAST:event_popAddSongActionPerformed

    private void lstPlayListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstPlayListMouseReleased
        if (evt.isPopupTrigger()) {
            popMenu.show(lstPlayList, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_lstPlayListMouseReleased

    private void popClearPlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popClearPlaylistActionPerformed
        mp3ListModel.clear();
    }//GEN-LAST:event_popClearPlaylistActionPerformed

    private void popDelSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popDelSongActionPerformed
        btnDeleteSongActionPerformed(evt);
    }//GEN-LAST:event_popDelSongActionPerformed

    private void popOpenPlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popOpenPlaylistActionPerformed
        menuOpenPlaylistActionPerformed(evt);
    }//GEN-LAST:event_popOpenPlaylistActionPerformed

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        System.exit(NORMAL);
    }//GEN-LAST:event_menuExitActionPerformed

    private void btnStopSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopSongActionPerformed
        player.stop();
        txtCurrentPlay.setText("stopped");
        txtCurrentPlayTime.setText("----");
        txtGeneralPlayTime.setText("----");
    }//GEN-LAST:event_btnStopSongActionPerformed

    private void btnPauseSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPauseSongActionPerformed
        player.pause();
    }//GEN-LAST:event_btnPauseSongActionPerformed

    private void sliderVolumeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderVolumeStateChanged
        player.setVolume(sliderVolume.getValue(), sliderVolume.getMaximum());
        
        if (sliderVolume.getValue() == 0) {
            tglbtnVolume.setSelected(true);
        }
        else {
            tglbtnVolume.setSelected(false);
        }
    }//GEN-LAST:event_sliderVolumeStateChanged

    private void lstPlayListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstPlayListMouseClicked
        if (evt.getModifiers() == InputEvent.BUTTON1_MASK && evt.getClickCount() == 2) {
           playFile();
        }
    }//GEN-LAST:event_lstPlayListMouseClicked

    private void lstPlayListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lstPlayListKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            playFile();
        }
    }//GEN-LAST:event_lstPlayListKeyPressed

    private void popPlaySongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popPlaySongActionPerformed
        btnPlaySongActionPerformed(evt);
    }//GEN-LAST:event_popPlaySongActionPerformed

    private void popPauseSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popPauseSongActionPerformed
        btnPauseSongActionPerformed(evt);
    }//GEN-LAST:event_popPauseSongActionPerformed

    private void popStopSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popStopSongActionPerformed
        btnStopSongActionPerformed(evt);
    }//GEN-LAST:event_popStopSongActionPerformed

    private void btnNextSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextSongActionPerformed
        if (selectNextSong()) {
            playFile();
        }
    }//GEN-LAST:event_btnNextSongActionPerformed

    private void btnPrevSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevSongActionPerformed
        if (selectPrevSong()) {
            playFile();
        }
    }//GEN-LAST:event_btnPrevSongActionPerformed

    private void slideProgressStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slideProgressStateChanged
        if (slideProgress.getValueIsAdjusting() == false) {
            if (moveAutomatic == true) {
                moveAutomatic = false;
                posValue = slideProgress.getValue() * 1.0 / 1000;
                processSeek(posValue);
            }
        } else {
            moveAutomatic = true;
            movingFromJump = true;
        }

    }//GEN-LAST:event_slideProgressStateChanged

    private void tglbtnVolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglbtnVolumeActionPerformed
        if (tglbtnVolume.isSelected()) {
            currentVolumeValue = sliderVolume.getValue();
            sliderVolume.setValue(0);
        } else {
            sliderVolume.setValue(currentVolumeValue);
        }
    }//GEN-LAST:event_tglbtnVolumeActionPerformed

    private void txtSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            searchSong();
        }
    }//GEN-LAST:event_txtSearchKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MP3PlayerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MP3PlayerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MP3PlayerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MP3PlayerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MP3PlayerGui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddSong;
    private javax.swing.JButton btnDeleteSong;
    private javax.swing.JButton btnNextSong;
    private javax.swing.JButton btnPauseSong;
    private javax.swing.JButton btnPlaySong;
    private javax.swing.JButton btnPrevSong;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSelectNext;
    private javax.swing.JButton btnSelectPrev;
    private javax.swing.JButton btnStopSong;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JList lstPlayList;
    private javax.swing.JMenu menuChangeSkin;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuOpenPlaylist;
    private javax.swing.JMenu menuPrefs;
    private javax.swing.JMenuItem menuSavePlaylist;
    private javax.swing.JPopupMenu.Separator menuSeparator;
    private javax.swing.JMenuItem menuSkin1;
    private javax.swing.JMenuItem menuSkin2;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JMenuItem popAddSong;
    private javax.swing.JMenuItem popClearPlaylist;
    private javax.swing.JMenuItem popDelSong;
    private javax.swing.JPopupMenu popMenu;
    private javax.swing.JMenuItem popOpenPlaylist;
    private javax.swing.JMenuItem popPauseSong;
    private javax.swing.JMenuItem popPlaySong;
    private javax.swing.JMenuItem popStopSong;
    public static javax.swing.JSlider slideProgress;
    private javax.swing.JSlider sliderVolume;
    private javax.swing.JToggleButton tglbtnVolume;
    private javax.swing.JLabel txtCurrentPlay;
    private javax.swing.JLabel txtCurrentPlayTime;
    private javax.swing.JLabel txtGeneralPlayTime;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables

}
