using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GamePad : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        float value = Input.GetAxis("Horizontal");
        if (value != 0)
            Debug.Log("horizontal " + value);

        value = Input.GetAxis("Vertical");

        if (value != 0)
         Debug.Log("vertical " + value);

        //string[] names = Input.GetJoystickNames();
    }
}
