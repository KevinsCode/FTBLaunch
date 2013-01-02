package net.ftb.gui.panes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.ftb.data.LauncherStyle;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.events.ModPackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.FilterDialogPacks;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.locale.I18N;
import net.ftb.locale.I18N.Locale;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

public class ModpacksPane extends JPanel implements ILauncherPane, ModPackListener {
	private static final long serialVersionUID = 1L;

	private static JPanel packs;
	public static ArrayList<JPanel> packPanels;
	private static JScrollPane packsScroll;
	private static JLabel splash;

	private static JLabel typeLbl;
	private JButton filter, editModPack;
	private static int selectedPack = 0;
	private static boolean modPacksAdded = false;
	private static HashMap<Integer, ModPack> currentPacks = new HashMap<Integer, ModPack>();
	private final ModpacksPane instance = this;
	private static JEditorPane packInfo;

	//	private JLabel loadingImage;
	public static String type = "Client", origin = "All", mcVersion = "All", avaliability = "All";
	public static boolean loaded = false;

	private static JScrollPane infoScroll;

	public ModpacksPane () {
		super();
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		splash = new JLabel();
		splash.setBounds(420, 0, 410, 200);
		add(splash);

		packPanels = new ArrayList<JPanel>();

		// TODO: Set loading animation while we wait
		//		try {
		//			loadingImage = new JLabel(new ImageIcon(new URL("http://cdn.nirmaltv.com/images/generatorphp-thumb.gif")));
		//		} catch (MalformedURLException e1) { e1.printStackTrace(); }
		//		loadingImage.setLocation(58, 36);

		packs = new JPanel();
		packs.setLayout(null);
		packs.setOpaque(false);

		// stub for a real wait message
		final JPanel p = new JPanel();
		p.setBounds(0, 0, 420, 55);
		p.setLayout(null);

		filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
		filter.setBounds(5, 5, 105, 25);
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(loaded) {
					FilterDialogPacks filterDia = new FilterDialogPacks(instance);
					filterDia.setVisible(true);
				}
			}
		});
		add(filter);

		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + type + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
		typeLblText += "</body></html>";

		typeLbl = new JLabel(typeLblText);
		typeLbl.setBounds(115, 5, 175, 25);
		typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		add(typeLbl);

		editModPack = new JButton(I18N.getLocaleString("MODS_EDIT_PACK"));
		editModPack.setBounds(300, 5, 110, 25);
		editModPack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(packPanels.size() > 0) {
					if(getSelectedModIndex() >= 0) {
						EditModPackDialog empd = new EditModPackDialog(LaunchFrame.getInstance());
						empd.setVisible(true);
					}
				}
			}
		});
		add(editModPack);

		JTextArea filler = new JTextArea(I18N.getLocaleString("MODS_WAIT_WHILE_LOADING"));
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(58, 6, 378, 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		//		p.add(loadingImage);
		p.add(filler);
		packs.add(p);

		packsScroll = new JScrollPane();
		packsScroll.setBounds(0, 30, 420, 280);
		packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		packsScroll.setWheelScrollingEnabled(true);
		packsScroll.setOpaque(false);
		packsScroll.setViewportView(packs);
		packsScroll.getVerticalScrollBar().setUnitIncrement(19);
		add(packsScroll);

		packInfo = new JEditorPane();
		packInfo.setEditable(false);
		packInfo.setContentType("text/html");
		packInfo.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					OSUtils.browse(event.getURL().toString());
				}
			}
		});
		packInfo.setBounds(420, 210, 410, 90);
		// TODO: Fix darker background for text area? Or is it better blending in?
		packInfo.setBackground(UIManager.getColor("control").darker().darker());
		add(packInfo);

		infoScroll = new JScrollPane();
		infoScroll.setBounds(420, 210, 410, 90);
		infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		infoScroll.setWheelScrollingEnabled(true);
		infoScroll.setViewportView(packInfo);
		infoScroll.setOpaque(false);
		add(infoScroll);
	}

	@Override 
	public void onVisible() { 
		if(!Settings.getSettings().getSnooper()) {
			LaunchFrame.tracker.trackPageViewFromReferrer("net/ftb/gui/ModpacksPane.java", "Modpacks Tab View", "Feed The Beast", "http://www.feed-the-beast.com", "/");
		}
	}

	/*
	 * GUI Code to add a modpack to the selection
	 */
	public static void addPack(ModPack pack) {
		if (!modPacksAdded) {
			modPacksAdded = true;
			packs.removeAll();
		}
		final int packIndex = packPanels.size();
		final JPanel p = new JPanel();
		p.setBounds(0, (packIndex * 55), 420, 55);
		p.setLayout(null);
		JLabel logo = new JLabel(new ImageIcon(pack.getLogo()));
		logo.setBounds(6, 6, 42, 42);
		logo.setVisible(true);

		JTextArea filler = new JTextArea(pack.getName() + " (v" + pack.getVersion() + ") Minecraft Version " + pack.getMcVersion() + "\n" + "By " + pack.getAuthor());
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(58, 6, 378, 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		MouseListener lin = new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {
				selectedPack = packIndex;
				updatePacks();
			}
			@Override public void mouseReleased(MouseEvent e) { }
			@Override public void mousePressed(MouseEvent e) { 
				selectedPack = packIndex;
				updatePacks();
			}
			@Override public void mouseExited(MouseEvent e) { }
			@Override public void mouseEntered(MouseEvent e) { }
		};
		p.addMouseListener(lin);
		filler.addMouseListener(lin);
		logo.addMouseListener(lin);
		p.add(filler);
		p.add(logo);
		packPanels.add(p);
		packs.add(p);
		if(currentPacks.isEmpty()) {
			packs.setMinimumSize(new Dimension(420, (ModPack.getPackArray().size() * 55)));
			packs.setPreferredSize(new Dimension(420, (ModPack.getPackArray().size() * 55)));
		} else {
			packs.setMinimumSize(new Dimension(420, (currentPacks.size() * 55)));
			packs.setPreferredSize(new Dimension(420, (currentPacks.size() * 55)));
		}
		packsScroll.revalidate();
		if(pack.getDir().equalsIgnoreCase(Settings.getSettings().getLastPack())) {
			selectedPack = packIndex;
		}
	}

	@Override
	public void onModPackAdded(ModPack pack) {
		addPack(pack);
		Logger.logInfo("Adding pack " + packPanels.size());
		updatePacks();
	}

	public static void sortPacks() {
		packPanels.clear();
		packs.removeAll();
		currentPacks.clear();
		int counter = 0;
		selectedPack = 0;
		packInfo.setText("");
		packs.repaint();
		for(ModPack pack : ModPack.getPackArray()) {
			if(originCheck(pack) && typeCheck(pack) && mcVersionCheck(pack) && avaliabilityCheck(pack) && textSearch(pack)) {
				currentPacks.put(counter, pack);
				addPack(pack);
				counter++;
			}
		}
		updatePacks();
	}

	private static void updatePacks() {
		for(int i = 0; i < packPanels.size(); i++) {
			if(selectedPack == i) {
				String mods = "";
				if(ModPack.getPack(getIndex()).getMods() != null) {
					mods += "<p>This pack contains the following mods by default:</p><ul>";
					for (String name : ModPack.getPack(getIndex()).getMods()) {
						mods += "<li>" + name + "</li>";
					}
					mods += "</ul>";
				}
				packPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
				splash.setIcon(new ImageIcon(ModPack.getPack(getIndex()).getImage()));
				packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				packInfo.setText(ModPack.getPack(getIndex()).getInfo() + mods);
				packInfo.setCaretPosition(0);
			} else {
				packPanels.get(i).setBackground(UIManager.getColor("control"));
				packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	public static int getSelectedModIndex() {
		return modPacksAdded ? getIndex() : -1;
	}

	public static void updateFilter() {
		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);
		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + type + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
		typeLblText += "</body></html>";

		typeLbl.setText(typeLblText);
		sortPacks();
		LaunchFrame.getInstance().updateFooter();
	}

	public static int getIndex() {
		return (!currentPacks.isEmpty()) ? currentPacks.get(selectedPack).getIndex() : selectedPack;
	}

	public void updateLocale() {
		filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
		editModPack.setText(I18N.getLocaleString("MODS_EDIT_PACK"));
		if(I18N.currentLocale == Locale.deDE) {
			editModPack.setBounds(290, 5, 120, 25);
			typeLbl.setBounds(115, 5, 165, 25);
		} else {
			editModPack.setBounds(300, 5, 110, 25);
			typeLbl.setBounds(115, 5, 175, 25);
		}
	}

	private static boolean avaliabilityCheck(ModPack pack) {
		return (avaliability.equalsIgnoreCase("all")) || (avaliability.equalsIgnoreCase("public") && !pack.isPrivatePack()) || (avaliability.equalsIgnoreCase("private") && pack.isPrivatePack());
	}

	private static boolean mcVersionCheck(ModPack pack) {
		return (mcVersion.equalsIgnoreCase("all")) || (mcVersion.equalsIgnoreCase(pack.getMcVersion()));
	}

	private static boolean typeCheck(ModPack pack) {
		return (type.equalsIgnoreCase("client")) || (type.equalsIgnoreCase("server") && !pack.getServerUrl().isEmpty());
	}

	private static boolean originCheck(ModPack pack) {
		return (origin.equalsIgnoreCase("all")) || (origin.equalsIgnoreCase("ftb") && pack.getAuthor().equalsIgnoreCase("the ftb team")) || (origin.equalsIgnoreCase("3rd party") && !pack.getAuthor().equalsIgnoreCase("the ftb team"));
	}

	private static boolean textSearch(ModPack pack) {
		String searchString = SearchDialog.lastPackSearch.toLowerCase();
		return ((searchString.isEmpty()) || pack.getName().toLowerCase().contains(searchString) || pack.getAuthor().toLowerCase().contains(searchString));
	}
}