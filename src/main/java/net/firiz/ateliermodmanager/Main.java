package net.firiz.ateliermodmanager;

import net.firiz.ateliermodmanager.file.FileManager;
import net.firiz.ateliermodmanager.gui.MainGUI;

public class Main {

    public static void main(String[] args) {
        FileManager.INSTANCE.load();
        final MainGUI window = new MainGUI();
        window.open();
    }

    /**
     * ライザ
     * pc20a おのぼり錬金術師 デフォルト
     * pc20h ミッドナイトブラック
     * pc20m 不思議な本の錬金術師
     * pc20g 常夏
     */

    /**
     * pc04 = Empel
     * pc05 = Lila
     * pc11 = Bos's dad
     * pc20 = Ryza
     * pc21 = Klaudia
     * pc22 = Lent
     *
     * pc23 = Toa
     * pc24 = Patty
     * pc25 = Cliff
     * pc26 = Serri
     */

    /**
     * PCxx = Playable/Player Character
     * xx = Character ID
     * ID 20 = Ryza
     *
     * PC20 = Ryza
     * PC20_X // where X is a letter will be Ryza's  Costume variations.
     * PC20_a // Default Costume
     * PC20_b // Costume Variation 1
     *
     * So also following this pattern
     * pc20_wep_01 // Ryza's Default Weapon
     */

    /**
     * new model blender tutorial
     * https://www.loverslab.com/topic/161226-atelier-ryza-2-lost-legends-the-secret-fairy-nude-mods/?do=findComment&comment=3281474
     */

}
