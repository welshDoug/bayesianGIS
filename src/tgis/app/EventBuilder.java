package tgis.app;

import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Event;
import java.awt.BorderLayout;
import java.awt.SystemColor;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JDialog;
import com.esri.arcgis.beans.map.MapBean;
import com.esri.arcgis.beans.TOC.TOCBean;
import javax.swing.AbstractAction;

import tgis.datatypes.RawDate;

/**
 * GUI for building up events, linking dates to locations in a shapefile.
 * So far only the loading is implemented, quite a bit more work is required to develop a linking UI and output format.
 */
public class EventBuilder {

	private JFrame jFrame = null;
	private JPanel jContentPane = null;
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu editMenu = null;
	private JMenu actionsMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private JMenuItem cutMenuItem = null;
	private JMenuItem copyMenuItem = null;
	private JMenuItem pasteMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JDialog aboutDialog = null;
	private JPanel aboutContentPane = null;
	private JLabel aboutVersionLabel = null;
	private MapBean mapBean = null;
	private TOCBean tocBean = null;
	private JList<RawDate> dateList = null;
	private JMenuItem loadShapefile = null;
	private JMenuItem loadDates = null;
	
	/**
	 * This method initializes jFrame
	 * 
	 * @return javax.swing.JFrame
	 */
	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setJMenuBar(getJJMenuBar());
			jFrame.setSize(700, 400);
			jFrame.setContentPane(getJContentPane());
			jFrame.setTitle("Application");
		}
		return jFrame;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getMapBean(), BorderLayout.CENTER);
			jContentPane.add(getTocBean(), BorderLayout.WEST);
			jContentPane.add(getDateList(), BorderLayout.EAST);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
			jJMenuBar.add(getEditMenu());
			jJMenuBar.add(getHelpMenu());
			jJMenuBar.setBackground(SystemColor.control);
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
			fileMenu.add(getLoadShapefile());
			fileMenu.add(getLoadDates());
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}
	
	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getActionsMenu() {
		if (actionsMenu == null) {
			actionsMenu = new JMenu();
			actionsMenu.setText("Actions");
		}
		return actionsMenu;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getEditMenu() {
		if (editMenu == null) {
			editMenu = new JMenu();
			editMenu.setText("Edit");
			editMenu.add(getCutMenuItem());
			editMenu.add(getCopyMenuItem());
			editMenu.add(getPasteMenuItem());
		}
		return editMenu;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			helpMenu.setText("Help");
			helpMenu.add(getAboutMenuItem());
		}
		return helpMenu;
	}
	
	/**
	 * This method initializes loadShapefile	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getLoadShapefile() {
		if (loadShapefile == null) {
			loadShapefile = new JMenuItem();
			loadShapefile.setText("Load Shapefile");
			loadShapefile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
					Event.CTRL_MASK, true));
			loadShapefile.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser c = new JFileChooser();
					c.setBackground(SystemColor.window);
				      // Demonstrate "Open" dialog:
				      int rVal = c.showOpenDialog(getJFrame());
				      if (rVal == JFileChooser.APPROVE_OPTION) {
				        try {
				        	getMapBean().clearLayers();
							getMapBean().addShapeFile(c.getCurrentDirectory().toString(), c.getSelectedFile().getName());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				      }
				      if (rVal == JFileChooser.CANCEL_OPTION) {
				      }
				}});
			
		}
		return loadShapefile;
	}
	
	/**
	 * This method initializes loadDates	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getLoadDates() {
		if (loadDates == null) {
			loadDates = new JMenuItem();
			loadDates.setText("Load Dates");
			loadDates.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
					Event.CTRL_MASK, true));
			loadDates.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser c = new JFileChooser();
					c.setBackground(SystemColor.window);
				      // Demonstrate "Open" dialog:
				      int rVal = c.showOpenDialog(getJFrame());
				      if (rVal == JFileChooser.APPROVE_OPTION) {
				        try {
				        	Vector<RawDate> dates = new Vector<RawDate>();
				        	BufferedReader file = new BufferedReader( new FileReader(c.getSelectedFile()));
							
							String dataRow = file.readLine();
							while (dataRow != null) {
								String[] dataLine = dataRow.split(",");
								String id = dataLine[0];
								long date = Long.parseLong(dataLine[1]);
								int sd = Integer.parseInt(dataLine[2]);
								dates.add(new RawDate(id, date, sd));
								
								dataRow = file.readLine();
							}
							
							dateList.setListData(dates);
							file.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				      }
				      if (rVal == JFileChooser.CANCEL_OPTION) {
				      }
				}});
			
		}
		return loadDates;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("Exit");
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
		}
		return exitMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			aboutMenuItem.setText("About");
			aboutMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JDialog aboutDialog = getAboutDialog();
					aboutDialog.pack();
					Point loc = getJFrame().getLocation();
					loc.translate(20, 20);
					aboutDialog.setLocation(loc);
					aboutDialog.setVisible(true);
				}
			});
		}
		return aboutMenuItem;
	}

	/**
	 * This method initializes aboutDialog	
	 * 	
	 * @return javax.swing.JDialog
	 */
	private JDialog getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new JDialog(getJFrame(), true);
			aboutDialog.setTitle("About");
			aboutDialog.setContentPane(getAboutContentPane());
		}
		return aboutDialog;
	}

	/**
	 * This method initializes aboutContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAboutContentPane() {
		if (aboutContentPane == null) {
			aboutContentPane = new JPanel();
			aboutContentPane.setLayout(new BorderLayout());
			aboutContentPane.add(getAboutVersionLabel(), BorderLayout.CENTER);
		}
		return aboutContentPane;
	}

	/**
	 * This method initializes aboutVersionLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getAboutVersionLabel() {
		if (aboutVersionLabel == null) {
			aboutVersionLabel = new JLabel();
			aboutVersionLabel.setText("Version 1.0");
			aboutVersionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return aboutVersionLabel;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCutMenuItem() {
		if (cutMenuItem == null) {
			cutMenuItem = new JMenuItem();
			cutMenuItem.setText("Cut");
			cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
					Event.CTRL_MASK, true));
		}
		return cutMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCopyMenuItem() {
		if (copyMenuItem == null) {
			copyMenuItem = new JMenuItem();
			copyMenuItem.setText("Copy");
			copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
					Event.CTRL_MASK, true));
		}
		return copyMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getPasteMenuItem() {
		if (pasteMenuItem == null) {
			pasteMenuItem = new JMenuItem();
			pasteMenuItem.setText("Paste");
			pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
					Event.CTRL_MASK, true));
		}
		return pasteMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem();
			saveMenuItem.setText("Save");
			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					Event.CTRL_MASK, true));
		}
		return saveMenuItem;
	}

	/**
	 * This method initializes mapBean	
	 * 	
	 * @return com.esri.arcgis.beans.map.MapBean	
	 */
	private MapBean getMapBean() {
		if (mapBean == null) {
			mapBean = new MapBean();
		}
		return mapBean;
	}

	/**
	 * This method initializes tocBean	
	 * 	
	 * @return com.esri.arcgis.beans.TOC.TOCBean	
	 */
	private TOCBean getTocBean() {
		if (tocBean == null) {
			tocBean = new TOCBean();
			try {
				tocBean.setBuddyControl(getMapBean());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tocBean;
	}
	
	/**
	 * This method initializes dateList	
	 * 	
	 * @return JList<RawDate>
	 */
	private JList<RawDate> getDateList() {
		if (dateList == null) {
			dateList = new JList<RawDate>();
		}
		return dateList;
	}

	void initializeArcGISLicenses() {
		try {
			com.esri.arcgis.system.AoInitialize ao = new com.esri.arcgis.system.AoInitialize();
			if (ao.isProductCodeAvailable(com.esri.arcgis.system.esriLicenseProductCode.esriLicenseProductCodeEngine) == com.esri.arcgis.system.esriLicenseStatus.esriLicenseAvailable)
				ao.initialize(com.esri.arcgis.system.esriLicenseProductCode.esriLicenseProductCodeEngine);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launches this application
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.esri.arcgis.system.EngineInitializer.initializeVisualBeans();
				EventBuilder application = new EventBuilder();
				application.getJFrame().setVisible(true);
			}
		});
	}
	
	public EventBuilder() {
		super();
		initializeArcGISLicenses();
	}

}
