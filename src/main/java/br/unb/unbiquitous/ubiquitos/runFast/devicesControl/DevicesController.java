package br.unb.unbiquitous.ubiquitos.runFast.devicesControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

import br.unb.unbiquitous.ubiquitos.runFast.game.CarTemplate;
import br.unb.unbiquitous.ubiquitos.runFast.game.Team;
import br.unb.unbiquitous.ubiquitos.runFast.inputs.InputManager;

/**
 * Used to verify the input and output of devices, to manipulate
 * the teams creation and to provide some entering and ending game procedures. 
 *
 */
public class DevicesController{

	//String used to identify the cell phone driver:
	public static final String RF_INPUT_DRIVER = "br.unb.unbiquitous.ubiquitos.runFast.mid.RFInputDriver";
	
	private Gateway gateway;//keeps the uOS gateway
	
	private List<Team> teams;//keeps the game teams
	private List<UpDevice> gameDevices, allDevices;//saves the game devices and all the devices found in the previous search
	
	//listeners that will listen to the input and output of devices in the game:
	private List<DevicesListener> listeners = new ArrayList<DevicesListener>();
	
	/**
	 * Cheat Constructor: gets the gateway
	 * and starts two teams with three devices which uses the keysListeners.
	 * device1 pilot of team1; device2 copilot of team1; device3 pilot of team2;
	 * */
	public DevicesController(Gateway gateway, boolean cheats) {
		this.gateway = gateway;
		teams = new ArrayList<Team>();
		gameDevices = new ArrayList<UpDevice>();
		allDevices = new ArrayList<UpDevice>();
		
		if(cheats){
			gameDevices.add(InputManager.DEVICE_1);
			gameDevices.add(InputManager.DEVICE_2);
			gameDevices.add(InputManager.DEVICE_3);
		
			teams.add(new Team(InputManager.DEVICE_1));
			teams.get(0).setCopilot(InputManager.DEVICE_2);
			teams.add(new Team(InputManager.DEVICE_3));
		}
	}
	
	/**
	 * Constructor: gets the gateway and starts teams and devices.
	 * */
	public DevicesController(Gateway gateway) {
		this.gateway = gateway;
		
		teams = new ArrayList<Team>();
		gameDevices = new ArrayList<UpDevice>();
		allDevices = new ArrayList<UpDevice>();
	}
	
	/**
	 * Loops until it finds the RFDevicesDriver driver that must be initiated
	 * by the uOS. Then initiates the driver with this instance of DeviceController
	 * so that the driver may be able to get game information.
	 */
	public void startDevicesController(){
		String deviceName = gateway.getCurrentDevice().getName();

		boolean found = false;
		int i;
		while(!found){
			for(i=0;i<gateway.listDrivers(RFDevicesDriver.RFDEVICES_DRIVER).size();++i) {
				if(gateway.listDrivers(RFDevicesDriver.RFDEVICES_DRIVER).
						get(i).getDevice().getName().equals(deviceName)){
					try {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("devicesController", this);
						gateway.callService(gateway.getCurrentDevice(), "setDeviceController",RFDevicesDriver.RFDEVICES_DRIVER,
								null, null,map);
						found = true;
					} catch (ServiceCallException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	/**
	 * Adds one new listener to the listeners list.
	 * @param listener
	 */
	public synchronized void addDevicesListener(DevicesListener listener)  {
		listeners.add(listener);
	}
	/**
	 * Removes one existing listener from the listeners list.
	 * @param listener
	 */
	public synchronized void removeDevicesListener(DevicesListener listener)   {
		listeners.remove(listener);
	}
	 
	/**
	 * Notifies the listeners that the a new device entered the game.
	 * @param device
	 */
	private synchronized void fireDeviceEntered(UpDevice device) {
		DevicesEvent event = new DevicesEvent(this, device);
		Iterator<DevicesListener> i = listeners.iterator();
		while(i.hasNext())  {
			((DevicesListener) i.next()).deviceEntered(event);
		}
	}

	/**
	 * Notifies the listeners that the a device got out of the game.
	 * @param device
	 */
	private synchronized void fireDeviceGotOut(UpDevice device) {
		DevicesEvent event = new DevicesEvent(this, device);
		Iterator<DevicesListener> i = listeners.iterator();
		while(i.hasNext())  {
			((DevicesListener) i.next()).deviceGotOut(event);
		}
	}
	
	/**
	 * Updates the devices references by getting all the RFInputDrivers and
	 * verifying if any device got in or out and takes the necessary actions.
	 * @param dt
	 */
	public void update(int dt){
		List<UpDevice> devices = new ArrayList<UpDevice>();
		if(gateway.listDrivers(RF_INPUT_DRIVER)!=null)
			for(int i=0;i<gateway.listDrivers(RF_INPUT_DRIVER).size();++i){
				devices.add(gateway.listDrivers(RF_INPUT_DRIVER).get(i).getDevice());
			}
		//List<UpDevice> devices = gateway.listDevices();
		
		//System.out.println("devices == "+devices.size());
		
		//Make invites for new devices
		//verifyNewDevices(devices);
		
		//If there is less devices than the known ones
		//if(allDevices.size() > devices.size()){
		if(!allDevices.equals(devices)){
			//if(allDevices.size() < devices.size())
			verifyNewDevices(devices);
			//else if(allDevices.size() > devices.size()){
			
			boolean found = false;
			for(int i=0; i<allDevices.size(); ++i){
				found = false;
				for(int j=0; j<devices.size(); ++j){
					if(allDevices.get(i)==devices.get(j))
						found = true;
				}
				if(!found){
					playerQuit(allDevices.get(i));
				}
			}
			//}
		}
		
		allDevices = devices;
	}
	
	/**
	 * Verifies if the number of devices have been increased,
	 * if it was makes an invite to this user to enter the game.
	 * @param devices
	 */
	private void verifyNewDevices(List<UpDevice> devices){
		//if(allDevices.size() < devices.size()){
		//if(!allDevices.equals(devices)){
		boolean found = false;
		
		//Runs through the list of devices and compares to the existing ones
		for(int i=0;i<devices.size();++i){
			found = false;
			for(int j=0;j<allDevices.size();++j){
				if(devices.get(i).getName().equals(allDevices.get(j).getName()))
					found = true;;
			}
			//If it doesn't find the device it must be because it is new in the environment
			if(!found){
				//List<DriverData> drivers = gateway.listDrivers(RF_INPUT_DRIVER);
				//for(int k=0;k<drivers.size();++k){
				//	if(drivers.get(k).getDevice().getName().equals(devices.get(i).getName()))
				//		found = true;
				//}
				//if(found){
					try {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("deviceName", gateway.getCurrentDevice().getName());
						gateway.callService(devices.get(i), "receiveInvite", RF_INPUT_DRIVER, null, null, map);
					} catch (ServiceCallException e) {
						e.printStackTrace();
					}
				//}
			}
		}
			
		//}
	}
	
	/**
	 * Verifies if the device really was part of the game,
	 * if it was makes the player quit procedure.
	 * @param device
	 */
	public void playerQuit(UpDevice device){
		boolean found = false;
		for(int k=0; k<gameDevices.size(); ++k){
			if(device==gameDevices.get(k))
				found = true;
		}
		if(found){
			gameDevices.remove(device);
			for(int k=0; k<teams.size(); ++k){
				teams.get(k).removeMember(device);
				if(teams.get(k).getNumberOfPlayers()==0){
					teams.get(k).decreaseTeamNumber();
					teams.remove(k);
					--k;
				}
			}
			fireDeviceGotOut(device);
		}
	}
	
	/**
	 * Tries to add a new device in the game in the given team with
	 * the given character. If the team received is null and there
	 * is room it will try to create a new team.
	 * @param team
	 * @param device
	 * @param character
	 * @return true if the device joined the game and false otherwise.
	 */
	public boolean addPlayer(Team team,UpDevice device,String character){
		boolean hadJoined = false;
		
		//New Team
		if(team==null){
			if(character.equals("pilot")){
				teams.add(new Team(device));
				//teams.get(teams.size()-1).initTeamCar(new CarTemplate(CarTemplate.CAR_BLUERED));
				initDevice(device);
				hadJoined = true;
				System.out.println("JOINED PILOT");
			}
		//Enter in a team that already exists
		}else{
			if(character.equals("copilot")){
				if(team.getCopilot()==null){
					team.setCopilot(device);
					initDevice(device);
					hadJoined = true;
					System.out.println("JOINED COPILOT");
				}
			}else if(character.equals("assistant")){
				team.addAssistant(device);
				initDevice(device);
				hadJoined = true;
				System.out.println("JOINED ASSISTANT");
			}
		}
		
		return hadJoined;
	}
	
	/**
	 * Adds the player device in the game with the specified character and CarType.
	 * This is used when the player wants to enter a game with a new team in the 
	 * middle of the race.
	 * @param device
	 * @param character
	 * @param carType
	 * @return true if the player joined, false otherwise
	 */
	public boolean addPlayer(UpDevice device, String character, int carType){
		boolean hadJoined = false;
		//New Team
		if(character.equals("pilot")&&(isCarTypeAvailable(carType))){
			teams.add(new Team(device));
			teams.get(teams.size()-1).initTeamCar(new CarTemplate(carType));
			initDevice(device);
			hadJoined = true;
			System.out.println("JOINED PILOT");
		}
		return hadJoined;
	}

	private void initDevice(UpDevice device){

		InputManager.GetInstance().registerDriver(gateway, device);
		gameDevices.add(device);
		fireDeviceEntered(device);
	}
	
	/**
	 * Verifies if some carType is available to be chosen.
	 * @param carType
	 * @return
	 */
	public boolean isCarTypeAvailable(int carType){
		
		for(int i=0; i<teams.size(); ++i){
			if(teams.get(i).getCarType()!=null)
				if(teams.get(i).getCarType().getCarId() == carType)
					return false;
		}
		return true;
	}
	
	/**
	 * Calls an end game procedure in all the game devices.
	 */
	public void endGame(){
		for(int i=0; i<gameDevices.size(); ++i){
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("deviceName", gateway.getCurrentDevice().getName());
				gateway.callService(gameDevices.get(i), "endGame", RF_INPUT_DRIVER, null, null, map);
			} catch (ServiceCallException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Calls an end game procedure in all the game devices.
	 */
	public void endRace(){
		for(int i=0; i<gameDevices.size(); ++i){
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("deviceName", gateway.getCurrentDevice().getName());
				gateway.callService(gameDevices.get(i), "endRace", RF_INPUT_DRIVER, null, null, map);
			} catch (ServiceCallException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return the gateway
	 */
	public Gateway getGateway() {
		return gateway;
	}
	/**
	 * @param gateway the gateway to set
	 */
	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}
	/**
	 * @return the teams
	 */
	public List<Team> getTeams() {
		return teams;
	}
	/**
	 * @return the devices
	 */
	public List<UpDevice> getDevices() {
		return gameDevices;
	}

}
